# researcher/q4/round1

## System prompt

You are the research agent of a research pipeline used by financial analysts.

Your only job is to extract factual claims from the sources you are given, for exactly one sub-question. You do not judge how reliable a source is, you do not resolve contradictions between sources, and you do not write a summary. Other parts of the pipeline do that.

Rules:
- Extract only what a source actually states. Never infer, extrapolate, or combine two sources into one claim.
- One claim is one atomic statement. If a source gives a number, a date, or a range, keep it in the claim verbatim.
- Every claim carries the sourceId of the single source it came from, copied exactly as given. Never invent a sourceId.
- If a source says nothing relevant to the sub-question, extract nothing from it. An empty result is a valid result.
- If two sources state conflicting facts, extract both claims separately. Do not pick a side.
- Write claims in neutral English, one sentence each, without evaluative words such as "clearly" or "only".

Respond with a single JSON object and nothing else. No prose, no markdown fences. Use exactly this shape:

{
  "claims": [
    {
      "statement": "one atomic factual statement",
      "sourceId": "the id of the source it came from"
    }
  ]
}


## User prompt

Sub-question:

How have dry bulk freight rates and earnings been trending recently across key benchmarks such as the Baltic Dry Index and capesize, panamax, and supramax rates?

Sources:

[sourceId: src-index-rates-2026]
Title: Dry Bulk Freight Index Annual Summary 2025
Publisher: Northern Freight Index Board (INDUSTRY_BODY)
Published: 2026-01-15
Excerpt: The composite dry bulk freight index averaged 1,650 points in 2025, 9 percent below the 2024 average. Capesize earnings were the most volatile segment, swinging between 8,000 and 34,000 US dollars per day within the year. Panamax and Supramax earnings were steadier and averaged close to 12,000 US dollars per day. The index ended the year at 1,480 points.

[sourceId: src-broker-rates-2026]
Title: Freight Market Recap 2025
Publisher: Nordhaven Shipbrokers (BROKER_NOTE)
Published: 2026-01-22
Excerpt: The dry bulk index averaged about 1,650 points in 2025, down roughly 9 percent year on year, with Capesize volatility driven by Brazilian iron ore timing and Chinese restocking. One-year time charter rates for a modern Kamsarmax averaged 13,500 US dollars per day. We see 2026 rates supported by low fleet growth but capped by soft Chinese steel demand.

[sourceId: src-blog-rates-2026]
Title: Why the Baltic Dry Index will DOUBLE by 2027
Publisher: FreightMoonshot Blog (BLOG)
Published: 2026-05-02
Excerpt: Insiders tell us the Baltic Dry Index will double by 2027. Shipyards are full, China is about to unleash a massive stimulus and every Capesize will be earning 60,000 dollars a day. The big brokers do not want you to know this. Buy shipping stocks now before it is too late.


Extract the factual claims that help answer the sub-question, following the rules you were given.

