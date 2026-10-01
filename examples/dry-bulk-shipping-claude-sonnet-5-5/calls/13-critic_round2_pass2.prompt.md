# critic/round2/pass2

## System prompt

You are the review agent of a research pipeline used by financial analysts. You act as an independent, adversarial reviewer. You did not write the briefing and you have no stake in it. Your job is to find what is wrong with it before an analyst relies on it.

You receive the briefing draft, the evidence groups it was allowed to use (each with a confidence level computed by the pipeline), and the list of sub-questions for which no adequate evidence was found.

Check the briefing for exactly these problems, in this order of importance:
1. UNSUPPORTED: a statement that is not backed by the evidence groups it cites, or that cites no group.
2. CONTRADICTS_EVIDENCE: a statement that says something different from what its cited groups say, including changed numbers, dates, or directions.
3. OVERSTATED_CERTAINTY: wording that adds certainty the cited groups do not carry. Calibration: a plain statement in the indicative mood ("the fleet grew 3.1 percent in 2025") is the correct register for MEDIUM and HIGH evidence and is not a finding. Flag only explicit certainty markers such as "clearly", "certainly", "undoubtedly", "confirmed", "will" for forecasts, or any LOW group presented as fact.
4. SMOOTHED_CONFLICT: two groups conflict, but the briefing presents one value, an average, or a resolution the evidence does not give. A sentence that names both values and both groups and calls the conflict open is the correct handling and is never a finding. Never ask the briefing to pick a base case or resolve a conflict; the evidence cannot do that.
5. MISSING_EVIDENCE: the analyst query clearly needs factual information that no evidence group covers and that is not already named as a gap. Formulate the missing piece as one research sub-question in suggestedQuestion and give 2 to 5 search keywords for it. Never use this type for wording, structure, or explanation problems; those are READABILITY.
6. READABILITY: a sentence a non-technical reader could not follow, or jargon without explanation.

Rules:
- Quote the exact text you object to in quotedText, copied from the briefing.
- Severity is MAJOR when an analyst could make a wrong decision because of it: a wrong or unsupported number, date, direction, or causal claim, or a hidden conflict. Disputes about emphasis words such as "main", "key", or "largest" are MINOR unless the evidence contradicts the emphasis.
- Do not invent evidence and do not propose new facts. You only compare the briefing with the evidence you were given.
- Do not report a gap that is already named as a gap in the briefing.
- Report at most 8 findings, the most severe first. If the same problem affects several sentences, report it once and name the pattern in detail instead of repeating it per sentence.
- If you find nothing wrong, return an empty findings list. An empty list is a valid and welcome result.

Respond with a single JSON object and nothing else. No prose, no markdown fences. Use exactly this shape:

{
  "findings": [
    {
      "type": "UNSUPPORTED | CONTRADICTS_EVIDENCE | OVERSTATED_CERTAINTY | SMOOTHED_CONFLICT | MISSING_EVIDENCE | READABILITY",
      "severity": "MAJOR | MINOR",
      "quotedText": "exact text from the briefing",
      "detail": "one or two sentences explaining the problem",
      "groupIds": ["ids of the evidence groups involved, may be empty"],
      "suggestedKeywords": ["only for MISSING_EVIDENCE, otherwise empty"],
      "suggestedQuestion": "only for MISSING_EVIDENCE: the sub-question to research, as one sentence; otherwise null"
    }
  ]
}


## User prompt

Analyst query:

Give me an overview of the dry bulk shipping market and its main risk drivers.

Briefing draft under review:

Summary:
- Seaborne dry bulk trade (bulk cargoes like ore, coal and grain moved by sea) grew 1.1 percent to 5.5 billion tonnes in 2025, with iron ore and coal making up 55 percent of volumes and grain and soybeans 10 percent. (groups: g-q1-c1, g-q1-c2, g-q1-c3)
- China is the dominant buyer, taking about 71 percent of seaborne iron ore imports, while its seaborne coal imports fell about 6 percent and Indian coal imports grew 4 percent. (groups: g-q1-c4, g-q2-c2, g-q2-c4)
- Freight rates were weaker in 2025: the main freight index averaged about 1,650 points, roughly 9 percent below 2024, and Capesize ships (the largest vessel size class) swung between 8,000 and 34,000 US dollars per day. (groups: g-q4-c1, g-q1-c20)
- On supply, Nordhaven Shipbrokers expects net fleet growth of 2.0 to 2.5 percent in 2026 and calls it manageable, but this is one broker's view, and the 2025 fleet growth figure is disputed (3.1 percent per the Maritime Statistics Bureau versus 2.4 percent per Nordhaven), so the starting point is unresolved. (groups: g-q3-c5, g-q3-c7, g-q3-c2, g-q1-c7, g-q3-c1)
- One broker, Hanseatic Maritime Research, lists six 2026 risk drivers: Chinese steel demand and property, Simandou ramp-up, grain flow shifts, port congestion, bunker (ship fuel) prices, and route disruption including Red Sea diversions. Separately, several brokers call a return to Suez routing the largest single downside risk to 2026 freight rates. (groups: g-q2-c14, g-q4-c11)

Key facts:
- Seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent, and iron ore is the largest single dry bulk commodity, ahead of coal and grain. (groups: g-q1-c12, g-q1-c15)
- China imported 1.15 billion tonnes of seaborne iron ore in 2025, about 71 percent of seaborne imports, while Australia supplied 56 percent and Brazil 24 percent of volumes. (groups: g-q1-c4, g-q1-c13, g-q1-c14)
- Capesize vessels account for 39 percent of dry bulk fleet capacity and Panamax for 25 percent, with Supramax and Handysize the remainder. These are vessel size classes, with Capesize the largest. (groups: g-q1-c10)
- Tonne-mile demand (tonnes multiplied by distance travelled) grew faster than tonnes in 2025. One source attributes this to longer Atlantic to Pacific routes, and another puts the growth at 2.3 percent, roughly double the growth in tonnes. (groups: g-q1-c6, g-q1-c19)
- A separate source says Cape of Good Hope diversions added between 1 and 2 percent to dry bulk tonne-mile demand in 2025. The evidence does not say how this relates to the longer Atlantic to Pacific routes cited above, so the two explanations are not reconciled. (groups: g-q4-c9, g-q1-c6)
- Chinese seaborne coal imports fell about 6 percent in 2025 as domestic production rose and hydro output recovered, weighing on Pacific Panamax demand. Nordhaven expects a further decline in 2026, and Indian coal imports grew 4 percent, partly offsetting the Chinese drop. (groups: g-q2-c2, g-q2-c3, g-q2-c4)
- A return to Suez routing would release the equivalent of roughly 2 percent of fleet capacity, and several brokers describe it as the largest single downside risk to 2026 freight rates. (groups: g-q4-c10, g-q4-c11)
- The dry bulk freight index averaged about 1,650 points in 2025, roughly 9 percent below 2024, and the composite index ended 2025 at 1,480 points. (groups: g-q4-c1, g-q4-c8)
- Capesize earnings were the most volatile in 2025, swinging between 8,000 and 34,000 US dollars per day, while Panamax and Supramax averaged close to 12,000 US dollars per day. Capesize volatility was linked to Brazilian iron ore timing and Chinese restocking. (groups: g-q1-c20, g-q1-c21, g-q4-c2)
- Nordhaven sees 2026 rates as supported by low fleet growth but capped by soft Chinese steel demand. Separately, industry participants expect deliveries of new ships to stay elevated in 2026 before easing in 2027; the evidence does not reconcile this with the low fleet growth view. (groups: g-q4-c4, g-q3-c16)

Uncertainties:
- Open conflict on 2025 fleet growth: the Maritime Statistics Bureau gives 3.1 percent and Nordhaven gives 2.4 percent. Nordhaven says it excludes vessels in long-term lay-up and counts conversions out of the fleet, which it says explains its lower figure. Its 2026 growth forecast of 2.0 to 2.5 percent and its 'manageable' supply view may rest on that narrower counting basis. Both 2025 figures are weakly supported. (groups: g-q1-c7, g-q3-c1, g-q3-c2, g-q3-c5, g-q3-c7)
- Open conflict on 2025 deliveries and removals: deliveries are 31.6 million dwt per the Maritime Statistics Bureau and 30.1 million dwt per Nordhaven, and demolition (scrapping) is 3.9 million dwt per the Bureau and 5.5 million dwt per Nordhaven. These figures are weakly supported and unresolved. (groups: g-q3-c9, g-q3-c10, g-q3-c3)
- Open conflict on the orderbook (ships ordered but not yet delivered): the Maritime Statistics Bureau puts it at 10.2 percent of the fleet and Nordhaven at 9.8 percent. The Bureau's claim that it is concentrated in Panamax and Supramax is weakly supported. (groups: g-q1-c9, g-q3-c4)
- The six risk drivers come from a single broker's list (Hanseatic Maritime Research), which is lower-tier evidence. Soft Chinese steel demand appears in the other evidence only as a factor capping 2026 rates, and the evidence does not rank or size the drivers. (groups: g-q2-c14, g-q4-c4)
- No evidence quantifies the 2026 outlook for demand (trade volumes or tonne-mile growth), so the risk drivers cannot be weighed against the supply outlook using the evidence provided. (groups: )
- No adequate evidence was found on the principal regulatory, geopolitical, and macroeconomic risks facing the dry bulk market, including IMO emissions rules, trade disputes, and shipping route disruptions, as a dedicated topic. (groups: )
- A forum poster claims an unpublished IMO rule will force half the dry bulk fleet into scrapping by 2027, while other posters say current carbon rules (CII and EEXI) only require slow steaming or efficiency upgrades. No poster cites an official document, so the claim is unconfirmed and should not be relied on. (groups: g-q5-c1, g-q5-c2, g-q5-c3)

Follow-up questions:
- Which 2025 fleet growth, deliveries, demolition, and orderbook figures are correct, and how do the Maritime Statistics Bureau and Nordhaven differ in what they count?
- What do the official IMO carbon intensity and efficiency rules actually require of dry bulk ships, and what is the expected effect on fleet supply?
- How much of the 2025 tonne-mile growth came from Cape of Good Hope diversions and how much from longer Atlantic to Pacific routes?
- What do published forecasts say about 2026 trade volumes and tonne-mile demand, so the risk drivers can be weighed against supply?
- How exposed is the market to trade disputes and wider macroeconomic weakness, especially in Chinese steel demand and property?

Evidence groups the briefing was allowed to use:

[g-q1-c1] confidence MEDIUM (0.60): Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c2] confidence MEDIUM (0.60): Iron ore and coal together made up 55 percent of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c3] confidence MEDIUM (0.60): Grain and soybeans made up 10 percent of seaborne dry bulk volumes in 2025, with minor bulks the remainder. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q2-c6] confidence MEDIUM (0.60): Iron ore and coal made up 55 percent of 2025 seaborne dry bulk volumes, grain and soybeans 10 percent, and minor bulks the remainder. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c4] confidence HIGH (0.75): China accounted for about 71 percent of seaborne iron ore imports in 2025. | 2 independent source(s), best tier A, newest 2026-04-09, conflict NONE
[g-q1-c5] confidence MEDIUM (0.60): Grain trade shifted towards South American exporters in 2025 after a weak North American harvest. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c6] confidence MEDIUM (0.60): Tonne-mile demand grew faster than tonnes in 2025 because of longer Atlantic to Pacific routes. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c7] confidence LOW (0.30): The world dry bulk fleet grew by 3.1 percent in 2025 to 1,020 million dwt, per the Maritime Statistics Bureau. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c1: The Maritime Statistics Bureau puts 2025 dry bulk fleet growth at 3.1 percent while Nordhaven puts it at 2.4 percent.
[g-q3-c9] confidence LOW (0.30): Dry bulk deliveries were 31.6 million dwt in 2025. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c3: 2025 dry bulk deliveries are reported as 31.6 million dwt by the Maritime Statistics Bureau and 30.1 million dwt by Nordhaven.
[g-q3-c10] confidence LOW (0.30): Dry bulk demolition was 3.9 million dwt in 2025 and stayed low because earnings remained above scrapping thresholds. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c3: 2025 dry bulk removals/demolition are reported as 3.9 million dwt by the Maritime Statistics Bureau and 5.5 million dwt by Nordhaven.
[g-q1-c9] confidence LOW (0.30): The dry bulk orderbook stood at 10.2 percent of the fleet at the end of 2025, concentrated in the Panamax and Supramax segments. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c4: The dry bulk orderbook is put at 10.2 percent of the fleet by the Maritime Statistics Bureau and at 9.8 percent by Nordhaven.
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
[g-q3-c1] confidence LOW (0.10): On Nordhaven's database the dry bulk fleet expanded by 2.4 percent in 2025. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c7: The Maritime Statistics Bureau puts 2025 dry bulk fleet growth at 3.1 percent while Nordhaven puts it at 2.4 percent.
[g-q3-c2] confidence MEDIUM (0.40): Nordhaven excludes vessels in long-term lay-up and counts conversions out of the fleet, which it says explains its lower fleet growth figure. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c3] confidence LOW (0.10): Nordhaven reports 2025 dry bulk deliveries of 30.1 million dwt and removals of 5.5 million dwt. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q3-c9, g-q3-c10: 2025 dry bulk deliveries are reported as 31.6 million dwt by the Maritime Statistics Bureau and 30.1 million dwt by Nordhaven.
[g-q3-c4] confidence LOW (0.10): Nordhaven estimates the dry bulk orderbook at 9.8 percent of the fleet. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c9: The dry bulk orderbook is put at 10.2 percent of the fleet by the Maritime Statistics Bureau and at 9.8 percent by Nordhaven.
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
[g-q5-c1] confidence LOW (0.00): A forum poster claims an unpublished IMO rule will force half of the dry bulk fleet into scrapping by 2027 because of carbon intensity ratings. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g-q5-c2: One poster claims an IMO rule will force half the fleet into scrapping by 2027, while others state that current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g-q5-c2] confidence LOW (0.00): Other forum posters state that current CII and EEXI rules only require slow steaming or efficiency upgrades. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g-q5-c1: One poster claims an IMO rule will force half the fleet into scrapping by 2027, while others state that current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g-q5-c3] confidence LOW (0.15): No poster in the forum thread cites an official document. | 1 independent source(s), best tier C, newest 2026-04-21, conflict NONE
[g-q1-c8] confidence MEDIUM (0.60): Dry bulk fleet deliveries reached 31.6 million dwt in 2025, while demolition was 3.9 million dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE

Sub-questions already declared as gaps:

[q5] What are the principal regulatory, geopolitical, and macroeconomic risks facing the dry bulk market, including IMO emissions rules, trade disputes, and shipping route disruptions?

Review the briefing following the rules you were given.

