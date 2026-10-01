# reconciler/round1

## System prompt

You are the reconciliation agent of a research pipeline used by financial analysts.

Your only job is to compare factual claims that were extracted from different sources for a set of related sub-questions, and to say which claims assert the same thing and which claims contradict each other. Claims collected for different sub-questions can still assert the same fact; group them together regardless of the sub-question they were collected for. You do not judge how reliable a source is, you do not decide who is right, and you do not write a summary.

Rules:
- Put claims into the same group when they assert the same fact about the same subject for the same period, even if the wording or the units differ.
- Two claims that give different values for the same quantity and the same period are a conflict between two groups. Do not merge them, even if they agree on the direction. Example: "fleet grew 3.1 percent in 2025" and "fleet grew 2.4 percent in 2025" are two groups and one conflict.
- A conflict exists only when the two claims cannot both be true. A percentage and an absolute number for the same thing, a qualitative statement and a number that fits it, or an average and a year-end level are different measures, not a conflict. If you would write "they do not actually contradict", do not list a conflict.
- Examples of claim pairs that are NOT a conflict: "China took 71 percent of seaborne iron ore imports" and "China imported 1.15 billion tonnes of iron ore"; "iron ore and coal together made up 55 percent of volumes" and "iron ore is the single largest commodity"; "the index averaged 1,650 points in 2025" and "the index ended 2025 at 1,480 points". Put such pairs in separate groups with no conflict.
- A forecast or trend from an older source that a later source reports as not having happened is a conflict. Example: "coal imports expected to keep growing" from 2019 against "coal imports fell 6 percent in 2025" from 2026. Keep both groups and record the conflict; the pipeline resolves recency itself.
- Claims about different periods, different regions, or different quantities are neither the same group nor a conflict. They are simply separate groups.
- Every claim id you receive must appear in exactly one group. A claim that matches nothing else forms a group on its own.
- Give each group a short id (g1, g2, ...) and one neutral sentence that states the shared assertion.
- A conflict references the ids of two or more different groups that contradict each other and explains the contradiction in one sentence, including the differing values. Never list the same group id twice: if the contradiction is between claims inside one group, split that group first and then reference the parts.

Before you answer, check every group you formed: if two of its claims give different numbers for the same quantity and the same period, split the group and record a conflict between the parts. Merging such claims hides a disagreement and inflates the confidence the pipeline computes from your grouping.

Respond with a single JSON object and nothing else. No prose, no markdown fences. Use exactly this shape:

{
  "groups": [
    {
      "id": "g1",
      "assertion": "one neutral sentence for the shared assertion",
      "claimIds": ["q1-c1", "q1-c3"]
    }
  ],
  "conflicts": [
    {
      "groupIds": ["g1", "g2"],
      "description": "one sentence naming the contradiction and the differing values"
    }
  ]
}


## User prompt

Sub-questions the claims were collected for:

[q1] What is the dry bulk shipping market, including its main vessel classes, cargo types, and principal trade routes?
[q2] What are the main demand drivers for dry bulk shipping volumes, such as iron ore, coal, grain, and industrial commodity trade?
[q3] What are the main supply-side drivers affecting dry bulk shipping capacity, including new vessel deliveries, fleet age, demolition, and orderbook levels?
[q4] How do freight rates and earnings in dry bulk shipping typically move, and what market indicators are used to track pricing and utilization?
[q5] What are the main risk drivers for dry bulk shipping, including geopolitical disruption, regulation, fuel costs, and macroeconomic slowdown?

Claims extracted from the sources (one per line, with the sub-question each claim was collected for):

[q1-c1] (q1; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): The world dry bulk fleet grew by 3.1 percent in 2025 to 1,020 million deadweight tonnes (dwt).
[q1-c2] (q1; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): The Bureau counts vessels above 10,000 dwt.
[q1-c3] (q1; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): Capesize vessels account for 39 percent of fleet capacity.
[q1-c4] (q1; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): Panamax vessels account for 25 percent of fleet capacity.
[q1-c5] (q1; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): Supramax and Handysize vessels account for the remainder of fleet capacity.
[q1-c6] (q1; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): The orderbook was concentrated in the Panamax and Supramax segments at year end.
[q1-c7] (q1; source src-broker-demand-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-04-09): China took roughly 70 to 71 percent of seaborne iron ore in 2025.
[q1-c8] (q1; source src-broker-demand-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-04-09): Chinese seaborne coal imports fell about 6 percent in 2025.
[q1-c9] (q1; source src-broker-demand-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-04-09): The decline in Chinese seaborne coal imports weighed on Panamax demand in the Pacific.
[q1-c10] (q1; source src-broker-demand-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-04-09): Indian coal imports grew 4 percent.
[q1-c11] (q1; source src-broker-rates-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-01-22): The dry bulk index averaged about 1,650 points in 2025.
[q1-c12] (q1; source src-broker-rates-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-01-22): One-year time charter rates for a modern Kamsarmax averaged 13,500 US dollars per day.
[q1-c13] (q1; source src-index-rates-2026, Northern Freight Index Board, INDUSTRY_BODY, 2026-01-15): The composite dry bulk freight index averaged 1,650 points in 2025.
[q1-c14] (q1; source src-index-rates-2026, Northern Freight Index Board, INDUSTRY_BODY, 2026-01-15): Capesize earnings swung between 8,000 and 34,000 US dollars per day within the year.
[q1-c15] (q1; source src-index-rates-2026, Northern Freight Index Board, INDUSTRY_BODY, 2026-01-15): Panamax and Supramax earnings averaged close to 12,000 US dollars per day.
[q2-c1] (q2; source src-ironore-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-01-28): Seaborne iron ore trade reached 1.62 billion tonnes in 2025.
[q2-c2] (q2; source src-ironore-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-01-28): China imported 1.15 billion tonnes of seaborne iron ore in 2025 and accounted for 71 percent of seaborne iron ore imports.
[q2-c3] (q2; source src-ironore-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-01-28): Iron ore remains the single largest dry bulk commodity, ahead of coal and grain.
[q2-c4] (q2; source src-industry-body-demand-2026, International Dry Cargo Association, INDUSTRY_BODY, 2026-03-05): Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025.
[q2-c5] (q2; source src-industry-body-demand-2026, International Dry Cargo Association, INDUSTRY_BODY, 2026-03-05): Iron ore and coal together made up 55 percent of seaborne dry bulk volumes in 2025.
[q2-c6] (q2; source src-industry-body-demand-2026, International Dry Cargo Association, INDUSTRY_BODY, 2026-03-05): Grain and soybeans made up 10 percent of seaborne dry bulk volumes in 2025.
[q2-c7] (q2; source src-industry-body-demand-2026, International Dry Cargo Association, INDUSTRY_BODY, 2026-03-05): Grain trade shifted towards South American exporters after a weak North American harvest.
[q2-c8] (q2; source src-industry-body-demand-2026, International Dry Cargo Association, INDUSTRY_BODY, 2026-03-05): Tonne-mile demand grew faster than tonnes because of longer Atlantic to Pacific routes.
[q2-c9] (q2; source src-risk-report-2026, Hanseatic Maritime Research, INDUSTRY_REPORT, 2026-05-20): Chinese steel demand and property construction are listed as main risk drivers for dry bulk earnings in 2026.
[q2-c10] (q2; source src-risk-report-2026, Hanseatic Maritime Research, INDUSTRY_REPORT, 2026-05-20): The ramp-up of the Simandou iron ore project in Guinea lengthens average haul distances.
[q2-c11] (q2; source src-risk-report-2026, Hanseatic Maritime Research, INDUSTRY_REPORT, 2026-05-20): Shifts in grain trade flows between the Americas and Asia are listed as a main risk driver for dry bulk earnings in 2026.
[q2-c12] (q2; source src-risk-report-2026, Hanseatic Maritime Research, INDUSTRY_REPORT, 2026-05-20): Tonne-mile demand grew 2.3 percent in 2025, roughly double the growth in tonnes.
[q2-c13] (q2; source src-broker-demand-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-04-09): Chinese seaborne coal imports fell about 6 percent in 2025 as domestic production rose and hydro output recovered.
[q2-c14] (q2; source src-broker-demand-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-04-09): Indian coal imports grew 4 percent in 2025.
[q2-c15] (q2; source src-broker-rates-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-01-22): Capesize volatility was driven by Brazilian iron ore timing and Chinese restocking.
[q2-c16] (q2; source src-broker-rates-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-01-22): 2026 rates were expected to be capped by soft Chinese steel demand.
[q3-c1] (q3; source src-broker-fleet-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-03-18): The dry bulk fleet expanded by 2.4 percent in 2025.
[q3-c2] (q3; source src-broker-fleet-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-03-18): Deliveries were 30.1 million dwt in 2025.
[q3-c3] (q3; source src-broker-fleet-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-03-18): Removals were 5.5 million dwt in 2025.
[q3-c4] (q3; source src-broker-fleet-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-03-18): The orderbook was 9.8 percent of the fleet.
[q3-c5] (q3; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): The world dry bulk fleet grew by 3.1 percent in 2025 to 1,020 million deadweight tonnes (dwt).
[q3-c6] (q3; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): Deliveries reached 31.6 million dwt in 2025.
[q3-c7] (q3; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): Demolition stayed low at 3.9 million dwt in 2025.
[q3-c8] (q3; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): The orderbook stood at 10.2 percent of the fleet at year end.
[q3-c9] (q3; source src-fleet-stats-2026, Maritime Statistics Bureau, OFFICIAL_STATISTICS, 2026-02-10): The Bureau counts vessels above 10,000 dwt.
[q3-c10] (q3; source src-tradepress-fleet-2026, Bulk Carrier Weekly, TRADE_PRESS, 2026-02-12): The dry bulk fleet grew 3.1 percent in 2025 to about 1,020 million dwt.
[q3-c11] (q3; source src-tradepress-fleet-2026, Bulk Carrier Weekly, TRADE_PRESS, 2026-02-12): Deliveries were 31.6 million dwt in 2025.
[q3-c12] (q3; source src-tradepress-fleet-2026, Bulk Carrier Weekly, TRADE_PRESS, 2026-02-12): There was very little scrapping in 2025.
[q3-c13] (q3; source src-tradepress-fleet-2026, Bulk Carrier Weekly, TRADE_PRESS, 2026-02-12): The orderbook was 10.2 percent of the fleet.
[q3-c14] (q3; source src-tradepress-fleet-2026, Bulk Carrier Weekly, TRADE_PRESS, 2026-02-12): Industry participants quoted in the report expect deliveries to stay elevated in 2026 before easing in 2027.
[q3-c15] (q3; source src-broker-fleet-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-03-18): The shipbroker expects net fleet growth of 2.0 to 2.5 percent in 2026.
[q4-c1] (q4; source src-index-rates-2026, Northern Freight Index Board, INDUSTRY_BODY, 2026-01-15): The dry bulk index averaged 1,650 points in 2025, 9 percent below the 2024 average.
[q4-c2] (q4; source src-index-rates-2026, Northern Freight Index Board, INDUSTRY_BODY, 2026-01-15): Capesize earnings were the most volatile segment, swinging between 8,000 and 34,000 US dollars per day within the year.
[q4-c3] (q4; source src-index-rates-2026, Northern Freight Index Board, INDUSTRY_BODY, 2026-01-15): Panamax and Supramax earnings were steadier and averaged close to 12,000 US dollars per day.
[q4-c4] (q4; source src-broker-rates-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-01-22): The dry bulk index averaged about 1,650 points in 2025, down roughly 9 percent year on year.
[q4-c5] (q4; source src-broker-rates-2026, Nordhaven Shipbrokers, BROKER_NOTE, 2026-01-22): One-year time charter rates for a modern Kamsarmax averaged 13,500 US dollars per day.
[q4-c6] (q4; source src-news-redsea-2026, Northern Maritime News, NEWS, 2026-07-11): Continued diversions around the Cape of Good Hope added between 1 and 2 percent to dry bulk tonne-mile demand in 2025.
[q4-c7] (q4; source src-news-redsea-2026, Northern Maritime News, NEWS, 2026-07-11): A return to Suez routing would release the equivalent of roughly 2 percent of fleet capacity.
[q4-c8] (q4; source src-risk-report-2026, Hanseatic Maritime Research, INDUSTRY_REPORT, 2026-05-20): Tonne-mile demand grew 2.3 percent in 2025, roughly double the growth in tonnes.
[q4-c9] (q4; source src-risk-report-2026, Hanseatic Maritime Research, INDUSTRY_REPORT, 2026-05-20): The main risk drivers for dry bulk earnings in 2026 include Chinese steel demand and property construction, the ramp-up of the Simandou iron ore project in Guinea, shifts in grain trade flows between the Americas and Asia, port congestion, bunker fuel prices, and geopolitical disruption of routes including Red Sea diversions.
[q5-c1] (q5; source src-risk-report-2026, Hanseatic Maritime Research, INDUSTRY_REPORT, 2026-05-20): Hanseatic Maritime Research identifies bunker fuel prices as one of six main risk drivers for dry bulk earnings in 2026.
[q5-c2] (q5; source src-risk-report-2026, Hanseatic Maritime Research, INDUSTRY_REPORT, 2026-05-20): Hanseatic Maritime Research identifies geopolitical disruption of routes including Red Sea diversions as one of six main risk drivers for dry bulk earnings in 2026.
[q5-c3] (q5; source src-risk-report-2026, Hanseatic Maritime Research, INDUSTRY_REPORT, 2026-05-20): Hanseatic Maritime Research identifies Chinese steel demand and property construction as one of six main risk drivers for dry bulk earnings in 2026.

Group the claims and name the conflicts, following the rules you were given.

