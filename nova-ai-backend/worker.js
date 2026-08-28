/**
 * Nova AI Coach — Cloudflare Worker backend
 *
 * Endpoints:
 *   POST /chat       — body: { "message": "user message" }
 *                      returns: { "reply": "AI response" }
 *   POST /scan-food  — body: { "image": "<base64 jpeg>", "mime": "image/jpeg" }
 *                      returns strict structured JSON:
 *                      {
 *                        "foods": [{ "name", "estimatedPortion", "calories",
 *                                    "proteinG", "carbsG", "fatG" }],
 *                        "totalCalories": number,
 *                        "confidence": "high" | "medium" | "low",
 *                        "disclaimer": "AI estimate — portions and calories may vary.",
 *                        "source": "nutrition_label" | "ai_estimate",
 *                        "per100": { "calories", "proteinG", "carbsG", "fatG" },  // optional
 *                        "portionGrams": number,  // optional
 *                        "basis": "per_100g" | "per_100ml" | "per_serving"  // optional
 *                      }
 *                      (foods is [] when no food is detected)
 *   OPTIONS *       — CORS preflight for Android/dev clients
 *
 * Secrets (set via Cloudflare dashboard or `wrangler secret put`):
 *   OPENAI_API_KEY — never hardcoded here.
 */

// CORS headers. "*" is fine for a mobile app backend (Android apps are not
// subject to browser CORS, but this also allows testing from web tools).
const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
  "Access-Control-Max-Age": "86400",
};

// Instructions that force the food-scan model to answer with strict JSON.
const SCAN_INSTRUCTIONS =
  "You are Nova's food photo analyzer. Analyze the food visible in the photo. " +
  "If a printed nutrition facts table or nutrition label is clearly readable, READ it " +
  "(kcal, protein, carbs, fat, serving size). Note the basis: per 100 g, per 100 ml, or per serving. " +
  "Prefer those printed numbers over visual guesses of the food. Never invent digits you cannot actually read. " +
  "If the detected or selected portion differs from the label basis, SCALE per-100g values as " +
  "value * (portionGrams / 100). Example: 59 kcal/100g x 150g = 88.5 which rounds to 89 kcal; " +
  "protein 10 x 1.5 = 15; carbs 3.3 x 1.5 ~ 5; fat 0.2 x 1.5 ~ 0.3. Fill foods[] with the " +
  "scaled per-portion numbers, and ALSO return the raw printed per-100 values so the server can recompute. " +
  "per100 must be the printed per-100 g (or per-100 ml) numbers, not per-serving numbers. " +
  "If the label is only per serving and serving grams are unknown, omit per100 and put the serving values in foods[]. " +
  "If no readable label (cooked meals, fruit, restaurant plates), estimate portions and macros from visible food. " +
  'Set source to "nutrition_label" when you read a label, or "ai_estimate" otherwise. ' +
  "Respond with STRICT JSON only — no markdown, no commentary — matching this schema: " +
  '{"foods":[{"name":"grilled chicken","estimatedPortion":"150 g",' +
  '"calories":250,"proteinG":40,"carbsG":0,"fatG":8}],"totalCalories":250,' +
  '"confidence":"medium","disclaimer":"AI estimate — portions and calories may vary.",' +
  '"source":"ai_estimate"}. Optional extra fields when a label was readable: ' +
  '"source":"nutrition_label","basis":"per_100g","portionGrams":150,' +
  '"per100":{"calories":59,"proteinG":10,"carbsG":3.3,"fatG":0.2}. ' +
  "Rules: list every distinct food visible; portions in natural units (150 g, 1 bowl, 2 slices); " +
  "calories in kcal; macros in grams; confidence is high/medium/low; " +
  "if there is no food in the photo, return " +
  '{"foods":[],"totalCalories":0,"confidence":"low","source":"ai_estimate",' +
  '"disclaimer":"AI estimate — portions and calories may vary."}. Never invent food that is not visible.';

const DEFAULT_DISCLAIMER = "AI estimate — portions and calories may vary.";
const CONFIDENCE_LEVELS = new Set(["high", "medium", "low"]);
const MAX_IMAGE_BASE64_LENGTH = 12_000_000; // ~9 MB binary, far above our ~500 KB uploads
const MAX_FOODS = 10;

/** Build a JSON Response with CORS headers attached. */
function jsonResponse(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json",
      ...CORS_HEADERS,
    },
  });
}

/**
 * Extract the assistant's text from a raw Responses API result.
 * The response body contains an `output` array; the assistant message
 * item holds `content` entries of type "output_text".
 */
function extractReply(data) {
  if (!Array.isArray(data.output)) return null;
  for (const item of data.output) {
    if (item.type === "message" && Array.isArray(item.content)) {
      for (const part of item.content) {
        if (part.type === "output_text" && typeof part.text === "string") {
          return part.text;
        }
      }
    }
  }
  return null;
}

/**
 * Pull the first JSON object out of the model's text. The model sometimes
 * wraps JSON in ```json fences; this strips them and keeps only the object.
 */
function extractJson(text) {
  const cleaned = text.replace(/```(?:json)?/gi, "").trim();
  const start = cleaned.indexOf("{");
  const end = cleaned.lastIndexOf("}");
  if (start === -1 || end <= start) return null;
  return cleaned.slice(start, end + 1);
}

/** Normalise a non-negative number (or 0 for garbage). */
function num(v) {
  return Number.isFinite(v) && v >= 0 ? v : 0;
}

function round1(v) {
  return Math.round(v * 10 + 1e-9) / 10;
}

function parseNonNegNumber(v) {
  if (typeof v === "string" && v.trim()) {
    const n = Number(v.trim().replace(",", "."));
    return Number.isFinite(n) && n >= 0 ? n : null;
  }
  return Number.isFinite(v) && v >= 0 ? v : null;
}

function parsePositiveNumber(v) {
  const n = parseNonNegNumber(v);
  return n !== null && n > 0 ? n : null;
}

function parsePer100(raw) {
  if (typeof raw !== "object" || raw === null || Array.isArray(raw)) return null;
  const calories = parseNonNegNumber(raw.calories);
  if (calories === null) return null;
  return {
    calories,
    proteinG: parseNonNegNumber(raw.proteinG) ?? 0,
    carbsG: parseNonNegNumber(raw.carbsG) ?? 0,
    fatG: parseNonNegNumber(raw.fatG) ?? 0,
  };
}

/** Deterministic per-100g scale. Do not trust the model to multiply. */
function scalePer100g(per100, portionGrams) {
  const grams = parsePositiveNumber(portionGrams);
  if (!grams || !per100) return null;
  const factor = grams / 100;
  return {
    calories: Math.round(per100.calories * factor),
    proteinG: round1(per100.proteinG * factor),
    carbsG: round1(per100.carbsG * factor),
    fatG: round1(per100.fatG * factor),
  };
}

function portionLabel(grams) {
  const text = Number.isInteger(grams) ? String(grams) : String(grams);
  return `${text} g`;
}

function applyScaledToFoods(foods, scaled, portionGrams) {
  const label = portionLabel(portionGrams);
  if (foods.length === 0) {
    foods.push({
      name: "food",
      estimatedPortion: label,
      calories: scaled.calories,
      proteinG: scaled.proteinG,
      carbsG: scaled.carbsG,
      fatG: scaled.fatG,
    });
    return foods;
  }
  const first = foods[0];
  foods[0] = {
    ...first,
    calories: scaled.calories,
    proteinG: scaled.proteinG,
    carbsG: scaled.carbsG,
    fatG: scaled.fatG,
    estimatedPortion: first.estimatedPortion || label,
  };
  return foods;
}

/**
 * Validate + sanitise the model's raw text into the strict scan schema.
 * Returns null when the text is not a JSON object at all.
 */
function sanitizeFoodScan(text) {
  const jsonText = extractJson(text);
  if (!jsonText) return null;

  let parsed;
  try {
    parsed = JSON.parse(jsonText);
  } catch {
    return null;
  }
  if (typeof parsed !== "object" || parsed === null || Array.isArray(parsed)) {
    return null;
  }

  const rawFoods = Array.isArray(parsed.foods) ? parsed.foods : [];
  const foods = rawFoods
    .slice(0, MAX_FOODS)
    .map((f) => {
      if (typeof f !== "object" || f === null) return null;
      return {
        name:
          typeof f.name === "string" && f.name.trim()
            ? f.name.trim().slice(0, 80)
            : "food",
        estimatedPortion:
          typeof f.estimatedPortion === "string"
            ? f.estimatedPortion.trim().slice(0, 40)
            : "",
        calories: Math.round(num(f.calories)),
        proteinG: num(f.proteinG),
        carbsG: num(f.carbsG),
        fatG: num(f.fatG),
      };
    })
    .filter((f) => f !== null);

  const source =
    parsed.source === "nutrition_label" || parsed.source === "ai_estimate"
      ? parsed.source
      : "ai_estimate";
  const per100 = parsePer100(parsed.per100);
  const portionGrams = parsePositiveNumber(parsed.portionGrams);
  const basis =
    typeof parsed.basis === "string" && parsed.basis.trim()
      ? parsed.basis.trim().slice(0, 40)
      : undefined;

  let scaled = null;
  if (source === "nutrition_label" && per100 && portionGrams) {
    scaled = scalePer100g(per100, portionGrams);
    if (scaled) applyScaledToFoods(foods, scaled, portionGrams);
  }

  const foodSum = foods.reduce((sum, f) => sum + f.calories, 0);
  const totalCalories = scaled
    ? foodSum
    : Math.round(
        Number.isFinite(parsed.totalCalories) && parsed.totalCalories >= 0
          ? parsed.totalCalories
          : foodSum
      );
  const confidence = CONFIDENCE_LEVELS.has(parsed.confidence)
    ? parsed.confidence
    : "medium";
  const disclaimer =
    typeof parsed.disclaimer === "string" && parsed.disclaimer.trim()
      ? parsed.disclaimer.trim().slice(0, 200)
      : DEFAULT_DISCLAIMER;

  const result = { foods, totalCalories, confidence, disclaimer, source };
  if (per100) result.per100 = per100;
  if (portionGrams) result.portionGrams = portionGrams;
  if (basis) result.basis = basis;
  return result;
}

/** POST /scan-food — analyze a food photo and return the strict scan schema. */
async function handleScanFood(request, env) {
  let body;
  try {
    body = await request.json();
  } catch {
    return jsonResponse({ error: "Invalid JSON body" }, 400);
  }

  const image = body?.image;
  const mime =
    typeof body?.mime === "string" && body.mime.trim() ? body.mime.trim() : "image/jpeg";
  if (typeof image !== "string" || image.trim().length === 0) {
    return jsonResponse({ error: 'Missing or empty "image" field (base64)' }, 400);
  }
  if (image.length > MAX_IMAGE_BASE64_LENGTH) {
    return jsonResponse({ error: "Image too large" }, 413);
  }

  // Guard against misconfigured deployments (secret not set).
  if (!env.OPENAI_API_KEY) {
    return jsonResponse({ error: "Server misconfigured" }, 500);
  }

  // --- Call the OpenAI Responses API with an image input ------------------
  let openaiResponse;
  try {
    openaiResponse = await fetch("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${env.OPENAI_API_KEY}`,
      },
      body: JSON.stringify({
        model: "gpt-4o-mini",
        instructions: SCAN_INSTRUCTIONS,
        input: [
          {
            role: "user",
            content: [
              { type: "input_image", image_url: `data:${mime};base64,${image}` },
              { type: "input_text", text: "Analyze the food in this photo. If a nutrition label is clearly readable, read those printed values first." },
            ],
          },
        ],
        max_output_tokens: 900,
      }),
    });
  } catch {
    // Network failure reaching OpenAI.
    return jsonResponse({ error: "Upstream request failed" }, 502);
  }

  if (!openaiResponse.ok) {
    // Log details server-side; return a generic error to the client
    // so we never leak key/quota details to the app.
    const errText = await openaiResponse.text();
    console.error("OpenAI API error", openaiResponse.status, errText);
    const status = openaiResponse.status === 429 ? 429 : 502;
    return jsonResponse({ error: "AI service error" }, status);
  }

  let data;
  try {
    data = await openaiResponse.json();
  } catch {
    return jsonResponse({ error: "Invalid upstream response" }, 502);
  }

  const scan = sanitizeFoodScan(extractReply(data) ?? "");
  if (!scan) {
    console.error("Unexpected food-scan AI response");
    return jsonResponse({ error: "Empty AI response" }, 502);
  }

  return jsonResponse(scan);
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    // --- CORS preflight -------------------------------------------------
    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }

    // --- Route check ----------------------------------------------------
    if (request.method === "POST" && url.pathname === "/scan-food") {
      return handleScanFood(request, env);
    }
    if (url.pathname !== "/chat") {
      return jsonResponse({ error: "Not found" }, 404);
    }
    if (request.method !== "POST") {
      return jsonResponse({ error: "Method not allowed. Use POST." }, 405);
    }

    // --- Parse and validate the request body ----------------------------
    let body;
    try {
      body = await request.json();
    } catch {
      return jsonResponse({ error: "Invalid JSON body" }, 400);
    }

    const message = body?.message;
    if (typeof message !== "string" || message.trim().length === 0) {
      return jsonResponse(
        { error: 'Missing or empty "message" field' },
        400
      );
    }

    // Guard against misconfigured deployments (secret not set).
    if (!env.OPENAI_API_KEY) {
      return jsonResponse({ error: "Server misconfigured" }, 500);
    }

    // --- Call the OpenAI Responses API ----------------------------------
    let openaiResponse;
    try {
      openaiResponse = await fetch("https://api.openai.com/v1/responses", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${env.OPENAI_API_KEY}`,
        },
        body: JSON.stringify({
          model: "gpt-4o-mini",
          instructions:
            "You are Nova, a supportive AI weight-loss and nutrition coach. " +
            "Give concise, practical, encouraging advice. You are not a " +
            "medical professional; suggest consulting a doctor for medical issues.",
          input: message,
          max_output_tokens: 600,
        }),
      });
    } catch {
      // Network failure reaching OpenAI.
      return jsonResponse({ error: "Upstream request failed" }, 502);
    }

    if (!openaiResponse.ok) {
      // Log details server-side; return a generic error to the client
      // so we never leak key/quota details to the app.
      const errText = await openaiResponse.text();
      console.error("OpenAI API error", openaiResponse.status, errText);
      const status = openaiResponse.status === 429 ? 429 : 502;
      return jsonResponse({ error: "AI service error" }, status);
    }

    // --- Extract the reply and return it --------------------------------
    let data;
    try {
      data = await openaiResponse.json();
    } catch {
      return jsonResponse({ error: "Invalid upstream response" }, 502);
    }

    const reply = extractReply(data);
    if (!reply) {
      console.error("Unexpected Responses API shape", JSON.stringify(data));
      return jsonResponse({ error: "Empty AI response" }, 502);
    }

    return jsonResponse({ reply });
  },
};