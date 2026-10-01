# researcher/q1/round1

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

What is the scope and structure of the dry bulk shipping market, including the main vessel segments, key cargoes, and major trade routes?

Sources:

[sourceId: src-industry-body-demand-2026]
Title: Dry Bulk Trade Annual Report 2025
Publisher: International Dry Cargo Association (INDUSTRY_BODY)
Published: 2026-03-05
Excerpt: Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. Iron ore and coal together made up 55 percent of volumes, grain and soybeans 10 percent, minor bulks the remainder. China accounted for 71 percent of seaborne iron ore imports in 2025. Grain trade shifted towards South American exporters after a weak North American harvest. Tonne-mile demand grew faster than tonnes because of longer Atlantic to Pacific routes.

[sourceId: src-fleet-stats-2026]
Title: World Dry Bulk Fleet Review 2025
Publisher: Maritime Statistics Bureau (OFFICIAL_STATISTICS)
Published: 2026-02-10
Excerpt: The world dry bulk fleet grew by 3.1 percent in 2025 to 1,020 million deadweight tonnes (dwt), the fastest expansion since 2021. Deliveries reached 31.6 million dwt while demolition stayed low at 3.9 million dwt because earnings remained above scrapping thresholds. The orderbook stood at 10.2 percent of the fleet at year end, concentrated in the Panamax and Supramax segments. Capesize vessels account for 39 percent of fleet capacity, Panamax for 25 percent, Supramax and Handysize for the remainder. The Bureau counts vessels above 10,000 dwt.

[sourceId: src-ironore-stats-2026]
Title: Seaborne Iron Ore Trade Statistics 2025
Publisher: Maritime Statistics Bureau (OFFICIAL_STATISTICS)
Published: 2026-01-28
Excerpt: Seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent on the year. China imported 1.15 billion tonnes and accounted for 71 percent of seaborne iron ore imports, unchanged from 2024 despite lower domestic steel output. Australia supplied 56 percent and Brazil 24 percent of seaborne volumes. Iron ore remains the single largest dry bulk commodity, ahead of coal and grain.

[sourceId: src-risk-report-2026]
Title: Dry Bulk Shipping Risk Drivers 2026
Publisher: Hanseatic Maritime Research (INDUSTRY_REPORT)
Published: 2026-05-20
Excerpt: We identify six main risk drivers for dry bulk earnings in 2026: Chinese steel demand and property construction, the ramp-up of the Simandou iron ore project in Guinea which lengthens average haul distances, shifts in grain trade flows between the Americas and Asia, port congestion, bunker fuel prices, and geopolitical disruption of routes including Red Sea diversions. Tonne-mile demand grew 2.3 percent in 2025, roughly double the growth in tonnes.

[sourceId: src-index-rates-2026]
Title: Dry Bulk Freight Index Annual Summary 2025
Publisher: Northern Freight Index Board (INDUSTRY_BODY)
Published: 2026-01-15
Excerpt: The composite dry bulk freight index averaged 1,650 points in 2025, 9 percent below the 2024 average. Capesize earnings were the most volatile segment, swinging between 8,000 and 34,000 US dollars per day within the year. Panamax and Supramax earnings were steadier and averaged close to 12,000 US dollars per day. The index ended the year at 1,480 points.


Extract the factual claims that help answer the sub-question, following the rules you were given.

