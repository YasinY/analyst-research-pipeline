# researcher/q3/round1

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

What are the main supply-side factors affecting dry bulk freight rates, including fleet size, orderbook, scrapping, and port congestion?

Sources:

[sourceId: src-news-redsea-2026]
Title: Red Sea detours still adding to bulk carrier demand, analysts say
Publisher: Northern Maritime News (NEWS)
Published: 2026-07-11
Excerpt: Analysts estimate that continued diversions around the Cape of Good Hope added between 1 and 2 percent to dry bulk tonne-mile demand in 2025. A return to Suez routing would release the equivalent of roughly 2 percent of fleet capacity, which several brokers describe as the largest single downside risk to freight rates in 2026.

[sourceId: src-risk-report-2026]
Title: Dry Bulk Shipping Risk Drivers 2026
Publisher: Hanseatic Maritime Research (INDUSTRY_REPORT)
Published: 2026-05-20
Excerpt: We identify six main risk drivers for dry bulk earnings in 2026: Chinese steel demand and property construction, the ramp-up of the Simandou iron ore project in Guinea which lengthens average haul distances, shifts in grain trade flows between the Americas and Asia, port congestion, bunker fuel prices, and geopolitical disruption of routes including Red Sea diversions. Tonne-mile demand grew 2.3 percent in 2025, roughly double the growth in tonnes.

[sourceId: src-container-2026]
Title: Container fleet growth hits 8 percent as freight rates slide
Publisher: Box Lines Review (TRADE_PRESS)
Published: 2026-03-30
Excerpt: The global container fleet grew 8 percent in 2025 to 32 million TEU as record deliveries met weakening demand on Asia to Europe routes. Spot freight rates for a forty-foot box fell 35 percent over the year. Liner operators responded with blank sailings and slower steaming. The container orderbook remains above 25 percent of the fleet.

[sourceId: src-broker-fleet-2026]
Title: Dry Bulk Supply Outlook, Spring 2026
Publisher: Nordhaven Shipbrokers (BROKER_NOTE)
Published: 2026-03-18
Excerpt: On our vessel database the dry bulk fleet expanded by 2.4 percent in 2025, below the 3 percent some agencies report, because we exclude vessels in long-term lay-up and count conversions out of the fleet. Deliveries were 30.1 million dwt and removals 5.5 million dwt. We estimate the orderbook at 9.8 percent of the fleet and expect net fleet growth of 2.0 to 2.5 percent in 2026 as shipyard slots remain tight and newbuilding prices stay high. Supply growth therefore looks manageable relative to demand.

[sourceId: src-tradepress-fleet-2026]
Title: Bulk fleet grew 3.1 percent last year, Bureau says
Publisher: Bulk Carrier Weekly (TRADE_PRESS)
Published: 2026-02-12
Excerpt: The Maritime Statistics Bureau reported this week that the dry bulk fleet grew 3.1 percent in 2025 to about 1,020 million dwt, with deliveries of 31.6 million dwt and very little scrapping. The Bureau put the orderbook at 10.2 percent of the fleet. Industry participants quoted in the report expect deliveries to stay elevated in 2026 before easing in 2027.


Extract the factual claims that help answer the sub-question, following the rules you were given.

