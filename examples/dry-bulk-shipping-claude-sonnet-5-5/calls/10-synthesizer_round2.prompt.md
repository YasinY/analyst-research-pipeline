# synthesizer/round2

## System prompt

You are the synthesis agent of a research pipeline used by financial analysts.

Your only job is to write a short briefing for a non-technical analyst from the evidence you are given. The evidence has already been collected, compared, and scored by other parts of the pipeline. You do not add knowledge of your own, you do not change the confidence scores, and you do not resolve conflicts that the evidence leaves open.

Rules:
- Every statement in summary, keyFacts, and uncertainties must cite the ids of the evidence groups it is based on, in groupIds. A statement without a groupId will be removed before the briefing is shown. Put the ids only in groupIds, never inside the text itself.
- The summary answers the analyst query directly in 3 to 5 sentences and touches every sub-question that has evidence, including the risk or outlook dimension if the query asks for it.
- Write 5 to 10 key facts that together cover every sub-question with eligible evidence. Qualitative findings from eligible groups, such as a named risk driver, are key facts too, not uncertainties.
- Uncertainties are reserved for three things: open conflicts between groups, weak evidence, and gaps. Do not describe two compatible facts as if they disagreed.
- Key facts may only be built from evidence groups listed under "Evidence eligible for key facts". Each key fact should rest on one group, or on several groups that agree.
- Groups listed under "Weak evidence" may only appear in uncertainties, phrased as unconfirmed or weakly supported, never as fact.
- Where two groups conflict, say so explicitly, give both values, and name both groups. Never average, pick a side, or smooth the conflict away.
- Evidence groups are formed per sub-question, so the same quantity can appear in one group without a conflict and in another group with an open conflict. Treat such a quantity as conflicted everywhere: do not state it as a key fact from the unconflicted group.
- Every sub-question listed under "Gaps" must appear in uncertainties with a plain statement that no adequate evidence was found for it.
- Use only numbers, dates, and names that appear in the evidence. Do not round, extrapolate, or add context from memory.
- Write in plain English for a reader without technical or domain background. Short sentences. No jargon without a short explanation.
- Suggest 3 to 5 follow-up questions an analyst could pursue next, focused on the gaps and conflicts.
- If you are given a previous draft and review findings, produce a corrected draft that resolves every finding. Keep what was not criticised.

Respond with a single JSON object and nothing else. No prose, no markdown fences. Use exactly this shape:

{
  "summary": [
    { "text": "one sentence", "groupIds": ["q1-g1"] }
  ],
  "keyFacts": [
    { "text": "one factual sentence", "groupIds": ["q1-g1"] }
  ],
  "uncertainties": [
    { "text": "one sentence naming a conflict, a weak finding, or a gap", "groupIds": ["q2-g1", "q2-g2"] }
  ],
  "followUpQuestions": [
    "a question an analyst could pursue next"
  ]
}

For a gap with no evidence group, use an empty groupIds list.


## User prompt

Analyst query:

Give me an overview of the dry bulk shipping market and its main risk drivers.

How the planner understood the query:

The analyst wants a structured overview of the dry bulk shipping market (its scope, demand and supply fundamentals, pricing, and outlook) with particular emphasis on the factors that create risk for market participants.

Evidence eligible for key facts:

[g-q1-c1] confidence MEDIUM (0.60): Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c2] confidence MEDIUM (0.60): Iron ore and coal together made up 55 percent of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c3] confidence MEDIUM (0.60): Grain and soybeans made up 10 percent of seaborne dry bulk volumes in 2025, with minor bulks the remainder. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q2-c6] confidence MEDIUM (0.60): Iron ore and coal made up 55 percent of 2025 seaborne dry bulk volumes, grain and soybeans 10 percent, and minor bulks the remainder. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c4] confidence HIGH (0.75): China accounted for about 71 percent of seaborne iron ore imports in 2025. | 2 independent source(s), best tier A, newest 2026-04-09, conflict NONE
[g-q1-c5] confidence MEDIUM (0.60): Grain trade shifted towards South American exporters in 2025 after a weak North American harvest. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c6] confidence MEDIUM (0.60): Tonne-mile demand grew faster than tonnes in 2025 because of longer Atlantic to Pacific routes. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c10] confidence MEDIUM (0.60): Capesize vessels account for 39 percent of dry bulk fleet capacity and Panamax for 25 percent, with Supramax and Handysize the remainder. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g-q1-c11] confidence MEDIUM (0.60): The Maritime Statistics Bureau counts dry bulk vessels above 10,000 dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g-q1-c12] confidence MEDIUM (0.60): Seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c13] confidence MEDIUM (0.60): China imported 1.15 billion tonnes of seaborne iron ore in 2025, 71 percent of seaborne imports and unchanged from 2024. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c14] confidence MEDIUM (0.60): Australia supplied 56 percent and Brazil 24 percent of seaborne iron ore volumes in 2025. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c15] confidence MEDIUM (0.60): Iron ore is the single largest dry bulk commodity, ahead of coal and grain. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c16] confidence MEDIUM (0.40): The Simandou iron ore ramp-up in Guinea lengthens average haul distances. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q1-c17] confidence MEDIUM (0.40): Hanseatic Maritime Research lists shifts in grain trade flows between the Americas and Asia as a 2026 risk driver for dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q1-c18] confidence MEDIUM (0.40): Hanseatic Maritime Research lists geopolitical route disruption, including Red Sea diversions, as a 2026 risk driver for dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q1-c19] confidence MEDIUM (0.40): Tonne-mile demand grew 2.3 percent in 2025, roughly double the growth in tonnes. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q1-c20] confidence MEDIUM (0.60): Capesize earnings swung between 8,000 and 34,000 US dollars per day in 2025, the most volatile segment. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g-q1-c21] confidence MEDIUM (0.60): Panamax and Supramax earnings averaged close to 12,000 US dollars per day in 2025, steadier than Capesize. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g-q2-c2] confidence MEDIUM (0.40): Chinese seaborne coal imports fell about 6 percent in 2025 as domestic production rose and hydro output recovered, weighing on Pacific Panamax demand. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c3] confidence MEDIUM (0.40): Nordhaven Shipbrokers expects Chinese coal imports to decline further in 2026. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c4] confidence MEDIUM (0.40): Indian coal imports grew 4 percent in 2025 and partly offset the Chinese decline. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c14] confidence MEDIUM (0.40): Hanseatic Maritime Research identifies six main risk drivers for 2026: Chinese steel demand and property, Simandou ramp-up, grain flow shifts, port congestion, bunker prices, and route disruption including Red Sea diversions. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q3-c2] confidence MEDIUM (0.40): Nordhaven excludes vessels in long-term lay-up and counts conversions out of the fleet, which it says explains its lower fleet growth figure. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c5] confidence MEDIUM (0.40): Nordhaven expects net dry bulk fleet growth of 2.0 to 2.5 percent in 2026. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c6] confidence MEDIUM (0.40): Nordhaven states that shipyard slots remain tight and newbuilding prices stay high. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c7] confidence MEDIUM (0.40): Nordhaven states that supply growth looks manageable relative to demand. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c16] confidence MEDIUM (0.40): Industry participants expect dry bulk deliveries to stay elevated in 2026 before easing in 2027. | 1 independent source(s), best tier B, newest 2026-02-12, conflict NONE
[g-q4-c1] confidence HIGH (0.75): The dry bulk freight index averaged about 1,650 points in 2025, roughly 9 percent below 2024. | 2 independent source(s), best tier A, newest 2026-01-22, conflict NONE
[g-q4-c2] confidence MEDIUM (0.40): Capesize volatility in 2025 was driven by Brazilian iron ore timing and Chinese restocking. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q4-c3] confidence MEDIUM (0.40): One-year time charter rates for a modern Kamsarmax averaged 13,500 US dollars per day in 2025. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q4-c4] confidence MEDIUM (0.40): Nordhaven sees 2026 rates as supported by low fleet growth but capped by soft Chinese steel demand. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q4-c8] confidence MEDIUM (0.60): The composite dry bulk freight index ended 2025 at 1,480 points. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g-q4-c9] confidence MEDIUM (0.40): Cape of Good Hope diversions added between 1 and 2 percent to dry bulk tonne-mile demand in 2025. | 1 independent source(s), best tier B, newest 2026-07-11, conflict NONE
[g-q4-c10] confidence MEDIUM (0.40): A return to Suez routing would release the equivalent of roughly 2 percent of fleet capacity. | 1 independent source(s), best tier B, newest 2026-07-11, conflict NONE
[g-q4-c11] confidence MEDIUM (0.40): Several brokers describe a return to Suez routing as the largest single downside risk to 2026 freight rates. | 1 independent source(s), best tier B, newest 2026-07-11, conflict NONE
[g-q1-c8] confidence MEDIUM (0.60): Dry bulk fleet deliveries reached 31.6 million dwt in 2025, while demolition was 3.9 million dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE

Weak evidence (may only appear in uncertainties):

[g-q1-c7] confidence LOW (0.30): The world dry bulk fleet grew by 3.1 percent in 2025 to 1,020 million dwt, per the Maritime Statistics Bureau. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c1: The Maritime Statistics Bureau puts 2025 dry bulk fleet growth at 3.1 percent while Nordhaven puts it at 2.4 percent.
[g-q3-c9] confidence LOW (0.30): Dry bulk deliveries were 31.6 million dwt in 2025. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c3: 2025 dry bulk deliveries are reported as 31.6 million dwt by the Maritime Statistics Bureau and 30.1 million dwt by Nordhaven.
[g-q3-c10] confidence LOW (0.30): Dry bulk demolition was 3.9 million dwt in 2025 and stayed low because earnings remained above scrapping thresholds. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c3: 2025 dry bulk removals/demolition are reported as 3.9 million dwt by the Maritime Statistics Bureau and 5.5 million dwt by Nordhaven.
[g-q1-c9] confidence LOW (0.30): The dry bulk orderbook stood at 10.2 percent of the fleet at the end of 2025, concentrated in the Panamax and Supramax segments. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c4: The dry bulk orderbook is put at 10.2 percent of the fleet by the Maritime Statistics Bureau and at 9.8 percent by Nordhaven.
[g-q3-c1] confidence LOW (0.10): On Nordhaven's database the dry bulk fleet expanded by 2.4 percent in 2025. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c7: The Maritime Statistics Bureau puts 2025 dry bulk fleet growth at 3.1 percent while Nordhaven puts it at 2.4 percent.
[g-q3-c3] confidence LOW (0.10): Nordhaven reports 2025 dry bulk deliveries of 30.1 million dwt and removals of 5.5 million dwt. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q3-c9, g-q3-c10: 2025 dry bulk deliveries are reported as 31.6 million dwt by the Maritime Statistics Bureau and 30.1 million dwt by Nordhaven.
[g-q3-c4] confidence LOW (0.10): Nordhaven estimates the dry bulk orderbook at 9.8 percent of the fleet. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c9: The dry bulk orderbook is put at 10.2 percent of the fleet by the Maritime Statistics Bureau and at 9.8 percent by Nordhaven.
[g-q5-c1] confidence LOW (0.00): A forum poster claims an unpublished IMO rule will force half of the dry bulk fleet into scrapping by 2027 because of carbon intensity ratings. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g-q5-c2: One poster claims an IMO rule will force half the fleet into scrapping by 2027, while others state that current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g-q5-c2] confidence LOW (0.00): Other forum posters state that current CII and EEXI rules only require slow steaming or efficiency upgrades. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g-q5-c1: One poster claims an IMO rule will force half the fleet into scrapping by 2027, while others state that current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g-q5-c3] confidence LOW (0.15): No poster in the forum thread cites an official document. | 1 independent source(s), best tier C, newest 2026-04-21, conflict NONE

Gaps (sub-questions without adequate evidence):

[q5] What are the principal regulatory, geopolitical, and macroeconomic risks facing the dry bulk market, including IMO emissions rules, trade disputes, and shipping route disruptions?

Revision context:

This is the first draft. There is no previous draft to revise.

Write the briefing following the rules you were given.

