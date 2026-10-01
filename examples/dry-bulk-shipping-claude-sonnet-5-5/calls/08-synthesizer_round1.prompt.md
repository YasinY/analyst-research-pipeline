# synthesizer/round1

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

The analyst wants a structured overview of the dry bulk shipping market (its scope, demand and supply fundamentals, freight rate behavior, and outlook) with emphasis on the factors that create risk for market participants.

Evidence eligible for key facts:

[g1] confidence MEDIUM (0.60): Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g2] confidence MEDIUM (0.60): Iron ore and coal together made up 55 percent of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g3] confidence MEDIUM (0.60): Grain and soybeans made up 10 percent of seaborne dry bulk volumes in 2025, with minor bulks making up the remainder. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g4] confidence MEDIUM (0.60): Iron ore and coal made up 55 percent, grain and soybeans 10 percent, and minor bulks the remainder of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g6] confidence MEDIUM (0.60): Capesize vessels account for 39 percent of dry bulk fleet capacity. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g7] confidence MEDIUM (0.60): Panamax vessels account for 25 percent of dry bulk fleet capacity. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g8] confidence MEDIUM (0.60): Supramax and Handysize together account for the remainder of fleet capacity after Capesize and Panamax. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g9] confidence MEDIUM (0.60): Capesize accounts for 39 percent and Panamax 25 percent of dry bulk fleet capacity, with Supramax and Handysize making up the remainder. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g11] confidence MEDIUM (0.60): The Maritime Statistics Bureau counts dry bulk vessels above 10,000 dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g12] confidence MEDIUM (0.60): Seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g13] confidence MEDIUM (0.60): Iron ore remains the single largest dry bulk commodity, ahead of coal and grain. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g14] confidence HIGH (0.90): China took about 71 percent of seaborne iron ore imports in 2025. | 3 independent source(s), best tier A, newest 2026-04-09, conflict NONE
[g15] confidence MEDIUM (0.40): Chinese seaborne coal imports fell about 6 percent in 2025. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g16] confidence MEDIUM (0.40): Nordhaven expects Chinese coal imports to decline further in 2026. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g17] confidence MEDIUM (0.40): Indian coal imports grew 4 percent in 2025, partly offsetting the Chinese decline. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g18] confidence MEDIUM (0.60): Grain trade shifted towards South American exporters after a weak North American harvest. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g19] confidence MEDIUM (0.60): Tonne-mile demand grew faster than tonnes in 2025 because of longer voyage distances. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g20] confidence MEDIUM (0.40): Hanseatic identifies six main risk drivers for dry bulk earnings in 2026: Chinese steel/property, Simandou ramp-up, grain flows, port congestion, bunker prices, and geopolitical route disruption including Red Sea diversions. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g21] confidence MEDIUM (0.40): The Simandou ramp-up in Guinea lengthens average haul distances. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g22] confidence HIGH (0.75): Dry bulk tonne-mile demand grew 2.3 percent in 2025, roughly double the growth in tonnes (1.1 percent). | 2 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g23] confidence MEDIUM (0.60): Australia supplied 56 percent and Brazil 24 percent of seaborne iron ore volumes in 2025. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g24] confidence MEDIUM (0.60): Red Sea/Suez diversions accounted for about 1 percentage point of 2025 tonne-mile growth. | 1 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g25] confidence MEDIUM (0.60): Average laden voyage distance reached 5,200 nautical miles in 2025. | 1 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g31] confidence MEDIUM (0.40): Nordhaven expects net dry bulk fleet growth of 2.0 to 2.5 percent in 2026. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g32] confidence MEDIUM (0.40): Nordhaven states dry bulk supply growth looks manageable relative to demand. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g33] confidence MEDIUM (0.40): Industry participants expect dry bulk deliveries to stay elevated in 2026 before easing in 2027. | 1 independent source(s), best tier B, newest 2026-02-12, conflict NONE
[g34] confidence HIGH (0.75): The composite dry bulk index averaged about 1,650 points in 2025, roughly 9 percent below 2024. | 2 independent source(s), best tier A, newest 2026-01-22, conflict NONE
[g35] confidence MEDIUM (0.40): Capesize volatility in 2025 was driven by Brazilian iron ore timing and Chinese restocking. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g36] confidence MEDIUM (0.40): One-year time charter rates for a modern Kamsarmax averaged 13,500 US dollars per day in 2025. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g37] confidence MEDIUM (0.40): Nordhaven sees 2026 rates supported by low fleet growth but capped by soft Chinese steel demand. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g38] confidence MEDIUM (0.60): Capesize earnings were the most volatile segment in 2025, swinging between 8,000 and 34,000 US dollars per day. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g39] confidence MEDIUM (0.60): Panamax and Supramax earnings averaged close to 12,000 US dollars per day in 2025. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g40] confidence MEDIUM (0.60): The composite dry bulk freight index ended 2025 at 1,480 points. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g41] confidence MEDIUM (0.40): The report lists Red Sea diversions as an example of geopolitical route disruption affecting dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE

Weak evidence (may only appear in uncertainties):

[g5] confidence LOW (0.30): The world dry bulk fleet grew 3.1 percent in 2025 to 1,020 million dwt (Maritime Statistics Bureau figure). | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g28: The Maritime Statistics Bureau puts 2025 dry bulk fleet growth at 3.1 percent while Nordhaven's database puts it at 2.4 percent.
[g10] confidence LOW (0.30): The dry bulk orderbook stood at 10.2 percent of the fleet at the end of 2025, concentrated in Panamax and Supramax. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g30: The dry bulk orderbook is 10.2 percent of the fleet per the Maritime Statistics Bureau but 9.8 percent per Nordhaven.
[g26] confidence LOW (0.30): Dry bulk deliveries reached 31.6 million dwt in 2025 (Maritime Statistics Bureau figure). | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g29: 2025 dry bulk deliveries are 31.6 million dwt per the Maritime Statistics Bureau but 30.1 million dwt per Nordhaven.
[g27] confidence LOW (0.30): Dry bulk demolition stayed low at 3.9 million dwt in 2025 because earnings remained above scrapping thresholds. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g29: 2025 fleet removals/demolition are 3.9 million dwt per the Maritime Statistics Bureau but 5.5 million dwt per Nordhaven.
[g28] confidence LOW (0.10): On Nordhaven's database the dry bulk fleet expanded 2.4 percent in 2025, excluding long-term lay-ups and counting conversions out. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g5: The Maritime Statistics Bureau puts 2025 dry bulk fleet growth at 3.1 percent while Nordhaven's database puts it at 2.4 percent.
[g29] confidence LOW (0.10): Nordhaven reports 2025 dry bulk deliveries of 30.1 million dwt and removals of 5.5 million dwt. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g26, g27: 2025 dry bulk deliveries are 31.6 million dwt per the Maritime Statistics Bureau but 30.1 million dwt per Nordhaven.
[g30] confidence LOW (0.10): Nordhaven estimates the dry bulk orderbook at 9.8 percent of the fleet. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g10: The dry bulk orderbook is 10.2 percent of the fleet per the Maritime Statistics Bureau but 9.8 percent per Nordhaven.
[g42] confidence LOW (0.00): A forum poster claims an unpublished IMO rule will force half the dry bulk fleet into scrapping by 2027. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g43: One forum poster says an IMO rule will force half the fleet to scrap by 2027, while others say current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g43] confidence LOW (0.00): Other forum posters state current CII and EEXI rules only require slow steaming or efficiency upgrades. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g42: One forum poster says an IMO rule will force half the fleet to scrap by 2027, while others say current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g44] confidence LOW (0.15): No poster in the thread cites an official document for the IMO rule claim. | 1 independent source(s), best tier C, newest 2026-04-21, conflict NONE

Gaps (sub-questions without adequate evidence):

(none)

Revision context:

This is the first draft. There is no previous draft to revise.

Write the briefing following the rules you were given.

