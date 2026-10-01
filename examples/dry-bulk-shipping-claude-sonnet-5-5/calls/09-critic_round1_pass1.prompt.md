# critic/round1/pass1

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
- Seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025, with iron ore and coal making up 55 percent of volumes and grain and soybeans 10 percent. (groups: g1, g2, g3)
- Tonne-mile demand (cargo moved multiplied by distance) grew 2.3 percent, about double the growth in tonnes, because voyages got longer, partly due to Red Sea and Suez diversions. (groups: g22, g24, g25)
- Freight rates were weaker overall: the composite index averaged about 1,650 points in 2025, roughly 9 percent below 2024, and Capesize earnings were the most volatile. (groups: g34, g38)
- On supply, Nordhaven expects net fleet growth of 2.0 to 2.5 percent in 2026 and calls supply growth manageable, while other industry participants expect deliveries to stay elevated in 2026 before easing in 2027. (groups: g31, g32, g33)
- Hanseatic names six main risk drivers for 2026 earnings, including Chinese steel and property demand, the Simandou iron ore ramp-up, grain flows, port congestion, bunker (ship fuel) prices and geopolitical route disruption. (groups: g20)

Key facts:
- Seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. (groups: g1)
- Iron ore and coal together made up 55 percent of seaborne dry bulk volumes in 2025, and grain and soybeans made up 10 percent, with minor bulks making up the rest. (groups: g2, g3, g4)
- Iron ore is the single largest dry bulk commodity; seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent. (groups: g12, g13)
- China took about 71 percent of seaborne iron ore imports in 2025, while Australia supplied 56 percent and Brazil 24 percent of seaborne iron ore volumes. (groups: g14, g23)
- Capesize vessels account for 39 percent and Panamax vessels 25 percent of dry bulk fleet capacity, with Supramax and Handysize making up the remainder. (groups: g6, g7, g8, g9)
- Dry bulk tonne-mile demand grew 2.3 percent in 2025, roughly double the 1.1 percent growth in tonnes; Red Sea and Suez diversions added about 1 percentage point, and average laden voyage distance reached 5,200 nautical miles. (groups: g22, g24, g25, g19)
- The composite dry bulk index averaged about 1,650 points in 2025, roughly 9 percent below 2024, and ended the year at 1,480 points. (groups: g34, g40)
- Capesize earnings swung between 8,000 and 34,000 US dollars per day in 2025, while Panamax and Supramax earnings averaged close to 12,000 US dollars per day. One source links Capesize swings to Brazilian iron ore timing and Chinese restocking. (groups: g38, g39, g35)
- Nordhaven sees 2026 rates supported by low fleet growth but capped by soft Chinese steel demand. (groups: g37)
- Hanseatic lists six main 2026 risk drivers: Chinese steel and property, Simandou ramp-up, grain flows, port congestion, bunker prices, and geopolitical route disruption such as Red Sea diversions. The Simandou ramp-up in Guinea lengthens average haul distances. (groups: g20, g21, g41)

Uncertainties:
- Fleet growth in 2025 is disputed: the Maritime Statistics Bureau says 3.1 percent, while Nordhaven's database says 2.4 percent. Both figures are weakly supported and neither can be treated as established. (groups: g5, g28)
- The orderbook (ships already ordered but not yet delivered) is put at 10.2 percent of the fleet by the Maritime Statistics Bureau and 9.8 percent by Nordhaven. The conflict is open. (groups: g10, g30)
- 2025 deliveries are 31.6 million dwt per the Maritime Statistics Bureau but 30.1 million dwt per Nordhaven. The conflict is open. (groups: g26, g29)
- 2025 demolition (ships scrapped) is 3.9 million dwt per the Maritime Statistics Bureau but 5.5 million dwt per Nordhaven. The conflict is open. (groups: g27, g29)
- A forum poster claims an unpublished IMO rule will force half the dry bulk fleet to scrap by 2027, while other posters say current CII and EEXI rules only require slow steaming or efficiency upgrades. This is unconfirmed, and no poster cites an official document. (groups: g42, g43, g44)
- The Nordhaven claim that supply growth looks manageable and the participant view that deliveries stay elevated in 2026 both rest on single, lower-tier sources. The underlying fleet-growth numbers are disputed, so the supply outlook is only weakly supported. (groups: g32, g33, g5, g28)
- The coal-demand findings (Chinese imports down about 6 percent in 2025, a further decline expected in 2026 per Nordhaven, Indian imports up 4 percent) each come from one lower-tier source, so they are only moderately supported. (groups: g15, g16, g17)

Follow-up questions:
- Which figures for 2025 fleet growth, deliveries and demolition are correct, and why do the Maritime Statistics Bureau and Nordhaven differ?
- Is the orderbook 10.2 percent or 9.8 percent of the fleet, and how much of it is due for delivery in 2026 versus 2027?
- Is there any official IMO document supporting a mass-scrapping requirement, or do only CII and EEXI efficiency rules apply?
- How much will the Simandou ramp-up add to iron ore volumes and haul distances in 2026?
- How would a further fall in Chinese coal and steel demand affect Capesize and Panamax earnings in 2026?

Evidence groups the briefing was allowed to use:

[g1] confidence MEDIUM (0.60): Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g2] confidence MEDIUM (0.60): Iron ore and coal together made up 55 percent of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g3] confidence MEDIUM (0.60): Grain and soybeans made up 10 percent of seaborne dry bulk volumes in 2025, with minor bulks making up the remainder. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g4] confidence MEDIUM (0.60): Iron ore and coal made up 55 percent, grain and soybeans 10 percent, and minor bulks the remainder of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g5] confidence LOW (0.30): The world dry bulk fleet grew 3.1 percent in 2025 to 1,020 million dwt (Maritime Statistics Bureau figure). | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g28: The Maritime Statistics Bureau puts 2025 dry bulk fleet growth at 3.1 percent while Nordhaven's database puts it at 2.4 percent.
[g6] confidence MEDIUM (0.60): Capesize vessels account for 39 percent of dry bulk fleet capacity. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g7] confidence MEDIUM (0.60): Panamax vessels account for 25 percent of dry bulk fleet capacity. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g8] confidence MEDIUM (0.60): Supramax and Handysize together account for the remainder of fleet capacity after Capesize and Panamax. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g9] confidence MEDIUM (0.60): Capesize accounts for 39 percent and Panamax 25 percent of dry bulk fleet capacity, with Supramax and Handysize making up the remainder. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g10] confidence LOW (0.30): The dry bulk orderbook stood at 10.2 percent of the fleet at the end of 2025, concentrated in Panamax and Supramax. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g30: The dry bulk orderbook is 10.2 percent of the fleet per the Maritime Statistics Bureau but 9.8 percent per Nordhaven.
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
[g26] confidence LOW (0.30): Dry bulk deliveries reached 31.6 million dwt in 2025 (Maritime Statistics Bureau figure). | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g29: 2025 dry bulk deliveries are 31.6 million dwt per the Maritime Statistics Bureau but 30.1 million dwt per Nordhaven.
[g27] confidence LOW (0.30): Dry bulk demolition stayed low at 3.9 million dwt in 2025 because earnings remained above scrapping thresholds. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g29: 2025 fleet removals/demolition are 3.9 million dwt per the Maritime Statistics Bureau but 5.5 million dwt per Nordhaven.
[g28] confidence LOW (0.10): On Nordhaven's database the dry bulk fleet expanded 2.4 percent in 2025, excluding long-term lay-ups and counting conversions out. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g5: The Maritime Statistics Bureau puts 2025 dry bulk fleet growth at 3.1 percent while Nordhaven's database puts it at 2.4 percent.
[g29] confidence LOW (0.10): Nordhaven reports 2025 dry bulk deliveries of 30.1 million dwt and removals of 5.5 million dwt. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g26, g27: 2025 dry bulk deliveries are 31.6 million dwt per the Maritime Statistics Bureau but 30.1 million dwt per Nordhaven.
[g30] confidence LOW (0.10): Nordhaven estimates the dry bulk orderbook at 9.8 percent of the fleet. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g10: The dry bulk orderbook is 10.2 percent of the fleet per the Maritime Statistics Bureau but 9.8 percent per Nordhaven.
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
[g42] confidence LOW (0.00): A forum poster claims an unpublished IMO rule will force half the dry bulk fleet into scrapping by 2027. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g43: One forum poster says an IMO rule will force half the fleet to scrap by 2027, while others say current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g43] confidence LOW (0.00): Other forum posters state current CII and EEXI rules only require slow steaming or efficiency upgrades. | 1 independent source(s), best tier C, newest 2026-04-21, conflict OPEN | conflicts with g42: One forum poster says an IMO rule will force half the fleet to scrap by 2027, while others say current CII and EEXI rules only require slow steaming or efficiency upgrades.
[g44] confidence LOW (0.15): No poster in the thread cites an official document for the IMO rule claim. | 1 independent source(s), best tier C, newest 2026-04-21, conflict NONE

Sub-questions already declared as gaps:

(none)

Review the briefing following the rules you were given.

