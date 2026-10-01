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

What is the dry bulk shipping market, what cargoes and vessel classes does it include, and how large is the market by fleet or trade volume?

Sources:

[sourceId: src-fleet-stats-2026]
Title: World Dry Bulk Fleet Review 2025
Publisher: Maritime Statistics Bureau (OFFICIAL_STATISTICS)
Published: 2026-02-10
Excerpt: The world dry bulk fleet grew by 3.1 percent in 2025 to 1,020 million deadweight tonnes (dwt), the fastest expansion since 2021. Deliveries reached 31.6 million dwt while demolition stayed low at 3.9 million dwt because earnings remained above scrapping thresholds. The orderbook stood at 10.2 percent of the fleet at year end, concentrated in the Panamax and Supramax segments. Capesize vessels account for 39 percent of fleet capacity, Panamax for 25 percent, Supramax and Handysize for the remainder. The Bureau counts vessels above 10,000 dwt.

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

[sourceId: src-blog-rates-2026]
Title: Why the Baltic Dry Index will DOUBLE by 2027
Publisher: FreightMoonshot Blog (BLOG)
Published: 2026-05-02
Excerpt: Insiders tell us the Baltic Dry Index will double by 2027. Shipyards are full, China is about to unleash a massive stimulus and every Capesize will be earning 60,000 dollars a day. The big brokers do not want you to know this. Buy shipping stocks now before it is too late.

[sourceId: src-forum-regulation-2026]
Title: Secret IMO rule will force half the bulk fleet to scrap in 2027 (thread)
Publisher: ShipTalk Forum (FORUM)
Published: 2026-04-21
Excerpt: A poster claims that an unpublished IMO rule will force half of the dry bulk fleet into scrapping by 2027 because of carbon intensity ratings. Other posters disagree and say the current CII and EEXI rules only require slow steaming or efficiency upgrades. Nobody in the thread cites an official document.


Extract the factual claims that help answer the sub-question, following the rules you were given.

