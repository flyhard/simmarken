#!/usr/bin/env python3
"""Generate app/src/main/assets/seed/simidrott.json from curated affisch/protocol text."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "app/src/main/assets/seed/simidrott.json"


def req(badge_code: str, n: int, text_sv: str, text_en: str) -> dict:
    return {
        "code": f"{badge_code}-{n:02d}",
        "textSv": text_sv,
        "textEn": text_en,
        "sortOrder": n,
    }


def badge(
    code: str,
    name_sv: str,
    name_en: str,
    sort_order: int,
    requirements: list[tuple[str, str]],
    image: bool = True,
) -> dict:
    image_path = f"badges/simidrott/{code.replace('-', '_')}.webp" if image else None
    return {
        "code": code,
        "nameSv": name_sv,
        "nameEn": name_en,
        "sortOrder": sort_order,
        "imageAssetPath": image_path,
        "requirements": [
            req(code, i + 1, sv, en) for i, (sv, en) in enumerate(requirements)
        ],
    }


def category(code: str, name_sv: str, name_en: str, sort_order: int, badges: list[dict]) -> dict:
    return {
        "code": code,
        "nameSv": name_sv,
        "nameEn": name_en,
        "sortOrder": sort_order,
        "badges": badges,
    }


CATALOG = {
    "code": "simidrott",
    "nameSv": "Svensk Simidrott",
    "nameEn": "Swedish Swimming",
    "catalogVersion": "2026.03.02",
    "sortOrder": 0,
    "categories": [
        category(
            "vattenvana",
            "Vattenvana",
            "Water confidence",
            1,
            [
                badge(
                    "baddaren-gron",
                    "Baddaren Grön",
                    "Baddaren Green",
                    1,
                    [
                        (
                            "Doppa hakan och ena örat under vattnet. Upprepa fem gånger för varje sida.",
                            "Dip your chin and one ear under the water. Repeat five times on each side.",
                        ),
                        (
                            "Bubbla genom att andas in, hålla andan och blås ut i vattenytan med hakan i vattnet, alternativt blåsa en pingisboll i vattenytan. Upprepa fem gånger.",
                            "Blow bubbles by breathing in, holding your breath, and blowing out at the surface with your chin in the water, or by blowing a ping-pong ball on the surface. Repeat five times.",
                        ),
                    ],
                ),
                badge(
                    "baddaren-bla",
                    "Baddaren Blå",
                    "Baddaren Blue",
                    2,
                    [
                        (
                            "Doppa huvudet under vattnet fem gånger.",
                            "Dip your head under the water five times.",
                        ),
                        (
                            "Bubbla genom att andas in, hålla andan och blås ut under vattnet. Upprepa fem gånger.",
                            "Blow bubbles by breathing in, holding your breath, and blowing out under the water. Repeat five times.",
                        ),
                        (
                            "Glid i vattnet i fem sekunder med framsträckta och raka armar, ansiktet ska vara under vattenytan. Frånskjut får göras och provet kan utföras på grunt vatten. Handstöd med platta får användas. Upprepa fem gånger.",
                            "Glide in the water for five seconds with straight arms extended and your face under the surface. A push-off is allowed and the test may be done in shallow water. Hand support with a kickboard is allowed. Repeat five times.",
                        ),
                    ],
                ),
                badge(
                    "baddaren-gul",
                    "Baddaren Gul",
                    "Baddaren Yellow",
                    3,
                    [
                        (
                            "Hoppa i från kant eller brygga på djupt vatten fem gånger.",
                            "Jump in from the pool edge or diving board in deep water five times.",
                        ),
                        (
                            "Bubbla genom att andas in, hålla andan och blås ut under vattnet. Upprepa fem gånger på djupt vatten. Märkestagaren får hålla i kant eller annat stöd mellan \"utblåsen\".",
                            "Blow bubbles by breathing in, holding your breath, and blowing out under the water. Repeat five times in deep water. The candidate may hold the edge or other support between exhalations.",
                        ),
                        (
                            "Glid i vattnet i fem sekunder med framsträckta och raka armar, ansiktet ska vara under vattenytan. Frånskjut får göras och provet ska utföras på djupt vatten. Handstöd med platta får användas. Upprepa fem gånger.",
                            "Glide in the water for five seconds with straight arms extended and your face under the surface. A push-off is allowed and the test must be done in deep water. Hand support with a kickboard is allowed. Repeat five times.",
                        ),
                    ],
                ),
                badge(
                    "skoldpaddan",
                    "Sköldpaddan",
                    "Turtle",
                    4,
                    [
                        (
                            "Flyt i 5 sekunder i ryggläge och gör därefter en rotation från ryggläge till magläge utan att röra botten. Provet ska utföras på grunt vatten.",
                            "Float for 5 seconds on your back and then rotate from back to front without touching the bottom. The test must be done in shallow water.",
                        ),
                    ],
                ),
                badge(
                    "blackfisken",
                    "Bläckfisken",
                    "Octopus",
                    5,
                    [
                        (
                            "Simma 5 m valfritt simsätt på grunt vatten.",
                            "Swim 5 m in any stroke in shallow water.",
                        ),
                    ],
                ),
            ],
        ),
        category(
            "nyborjare",
            "Nybörjare",
            "Beginner",
            2,
            [
                badge(
                    "pingvinen-silver",
                    "Pingvinen Silver",
                    "Penguin Silver",
                    1,
                    [
                        (
                            "Simma 10 m valfritt simsätt på grunt vatten.",
                            "Swim 10 m in any stroke in shallow water.",
                        ),
                        (
                            "Flyt 10 sekunder på grunt vatten.",
                            "Float for 10 seconds in shallow water.",
                        ),
                        (
                            "Hoppa från kant eller brygga på grunt vatten.",
                            "Jump from the pool edge or diving board in shallow water.",
                        ),
                        (
                            "Doppa huvudet genom att hämta föremål från botten med båda händerna på grunt vatten.",
                            "Dip your head by retrieving an object from the bottom with both hands in shallow water.",
                        ),
                    ],
                ),
                badge(
                    "pingvinen-guld",
                    "Pingvinen Guld",
                    "Penguin Gold",
                    2,
                    [
                        (
                            "Simma 10 m valfritt simsätt i magläge på djupt vatten.",
                            "Swim 10 m in any stroke on your front in deep water.",
                        ),
                        (
                            "Simma 10 m i ryggläge på djupt vatten.",
                            "Swim 10 m on your back in deep water.",
                        ),
                        (
                            "Flyt 10 sekunder på djupt vatten.",
                            "Float for 10 seconds in deep water.",
                        ),
                        (
                            "Hoppa från kant eller brygga på djupt vatten.",
                            "Jump from the pool edge or diving board in deep water.",
                        ),
                    ],
                ),
                badge(
                    "simsattmarke-1",
                    "Simsättsmärke 1",
                    "Stroke Badge 1",
                    3,
                    [
                        (
                            "Simma 10 m ryggsim.",
                            "Swim 10 m backstroke.",
                        ),
                    ],
                    image=False,
                ),
                badge(
                    "simsattmarke-2",
                    "Simsättsmärke 2",
                    "Stroke Badge 2",
                    4,
                    [
                        (
                            "Simma 10 m i magläge (crawl, bröstsim eller fjärilsim).",
                            "Swim 10 m on your front (front crawl, breaststroke, or butterfly).",
                        ),
                    ],
                    image=False,
                ),
                badge(
                    "silverfisken",
                    "Silverfisken",
                    "Silverfish",
                    5,
                    [
                        (
                            "Simma 25 m valfritt simsätt på grunt vatten.",
                            "Swim 25 m in any stroke in shallow water.",
                        ),
                        (
                            "Hoppa från kant eller brygga på grunt vatten.",
                            "Jump from the pool edge or diving board in shallow water.",
                        ),
                    ],
                ),
                badge(
                    "guldfisken",
                    "Guldfisken",
                    "Goldfish",
                    6,
                    [
                        (
                            "Simma 25 m valfritt simsätt på djupt vatten.",
                            "Swim 25 m in any stroke in deep water.",
                        ),
                        (
                            "Dyk från kant eller brygga på djupt vatten.",
                            "Dive from the pool edge or diving board in deep water.",
                        ),
                    ],
                ),
            ],
        ),
        category(
            "hajen",
            "Hajen",
            "Shark",
            3,
            [
                badge(
                    "hajen-brons",
                    "Hajen Brons",
                    "Shark Bronze",
                    1,
                    [
                        (
                            "Simma 100 m valfritt simsätt på djupt vatten.",
                            "Swim 100 m in any stroke in deep water.",
                        ),
                    ],
                ),
                badge(
                    "hajen-silver",
                    "Hajen Silver",
                    "Shark Silver",
                    2,
                    [
                        (
                            "Simma 100 m valfritt simsätt varav 25 m i ryggläge på djupt vatten.",
                            "Swim 100 m in any stroke, including 25 m on your back, in deep water.",
                        ),
                        (
                            "Simma 12,5 m i magläge, gör en rotation från magläge till ryggläge utan att röra botten och simma 12,5 m i ryggläge.",
                            "Swim 12.5 m on your front, rotate from front to back without touching the bottom, and swim 12.5 m on your back.",
                        ),
                    ],
                ),
                badge(
                    "hajen-guld",
                    "Hajen Guld",
                    "Shark Gold",
                    3,
                    [
                        (
                            "Simma 200 m valfritt simsätt varav 50 m i ryggläge på djupt vatten.",
                            "Swim 200 m in any stroke, including 50 m on your back, in deep water.",
                        ),
                        (
                            "Hoppa eller dyk från 1 m.",
                            "Jump or dive from 1 m.",
                        ),
                        (
                            "Livräddning: Kunna hjälpa en nödställd person med förlängda armen.",
                            "Lifesaving: Be able to help a person in distress using the extended arm technique.",
                        ),
                        (
                            "Kunskap om punkterna 1-3 i bad- och båtvett.",
                            "Knowledge of points 1–3 in swimming and boating safety.",
                        ),
                        (
                            "Flyt med kläder: 1 minut eller 10 m. Klädsel: långärmad överdel och långbyxor.",
                            "Float with clothes: 1 minute or 10 m. Clothing: long-sleeved top and long trousers.",
                        ),
                        (
                            "Simma 12,5 m i magläge, gör en rotation från magläge till ryggläge utan att röra botten, simma 12,5 m i ryggläge.",
                            "Swim 12.5 m on your front, rotate from front to back without touching the bottom, and swim 12.5 m on your back.",
                        ),
                    ],
                ),
                badge(
                    "simsattmarke-3",
                    "Simsättsmärke 3",
                    "Stroke Badge 3",
                    4,
                    [
                        ("Crawl", "Front crawl"),
                        ("Bröstsim", "Breaststroke"),
                        ("Fjärilsim", "Butterfly"),
                    ],
                    image=False,
                ),
                badge(
                    "simsattmarke-4",
                    "Simsättsmärke 4",
                    "Stroke Badge 4",
                    5,
                    [
                        (
                            "Start från kant eller startpall i alla fyra simsätt, inklusive uppgång och simma tre simtag.",
                            "Start from the edge or starting block in all four strokes, including surfacing and swimming three strokes.",
                        ),
                        (
                            "Simma 100 m medley.",
                            "Swim 100 m individual medley.",
                        ),
                    ],
                    image=False,
                ),
            ],
        ),
        category(
            "jarn",
            "Järn",
            "Iron",
            4,
            [
                badge(
                    "jarnmarket",
                    "Järnmärket",
                    "Iron Badge",
                    1,
                    [
                        (
                            "Simma 50 m valfritt simsätt i magläge på djupt vatten.",
                            "Swim 50 m in any stroke on your front in deep water.",
                        ),
                        (
                            "Simma 25 m i ryggläge på djupt vatten.",
                            "Swim 25 m on your back in deep water.",
                        ),
                        (
                            "Flyt 1 minut eller 10 m.",
                            "Float for 1 minute or 10 m.",
                        ),
                        (
                            "Dyk från kant eller brygga på djupt vatten.",
                            "Dive from the pool edge or diving board in deep water.",
                        ),
                    ],
                ),
            ],
        ),
        category(
            "brons",
            "Brons",
            "Bronze",
            5,
            [
                badge(
                    "bronsmarket",
                    "Bronsmärket",
                    "Bronze Badge",
                    1,
                    [
                        (
                            "Simma 100 m valfritt simsätt i magläge på djupt vatten.",
                            "Swim 100 m in any stroke on your front in deep water.",
                        ),
                        (
                            "Simma 50 m i ryggläge på djupt vatten.",
                            "Swim 50 m on your back in deep water.",
                        ),
                        (
                            "Flyt 1 minut och 30 sekunder eller 20 m.",
                            "Float for 1 minute and 30 seconds or 20 m.",
                        ),
                        (
                            "Längddyk 5 m.",
                            "Glide underwater for 5 m.",
                        ),
                        (
                            "Livräddning: Kunskap om punkterna 1-3 i bad- och båtvett. Kunna hjälpa en nödställd person med förlängda armen. Kast av livboj med lina från kanten till nödställd i vattnet.",
                            "Lifesaving: Knowledge of points 1–3 in swimming and boating safety. Be able to help a person in distress with the extended arm. Throw a lifeline buoy from the edge to a person in the water.",
                        ),
                    ],
                ),
            ],
        ),
        category(
            "silver",
            "Silver",
            "Silver",
            6,
            [
                badge(
                    "silvermarket",
                    "Silvermärket",
                    "Silver Badge",
                    1,
                    [
                        (
                            "Simma 300 m valfritt simsätt i magläge på djupt vatten.",
                            "Swim 300 m in any stroke on your front in deep water.",
                        ),
                        (
                            "Simma 150 m i ryggläge på djupt vatten.",
                            "Swim 150 m on your back in deep water.",
                        ),
                        (
                            "Flyt 2 minuter eller 50 m.",
                            "Float for 2 minutes or 50 m.",
                        ),
                        (
                            "Flyt med kläder: 1 minut och 30 sekunder eller 20 m. Klädsel: långärmad överdel och långbyxor.",
                            "Float with clothes: 1 minute and 30 seconds or 20 m. Clothing: long-sleeved top and long trousers.",
                        ),
                        (
                            "Längddyk 8 m.",
                            "Glide underwater for 8 m.",
                        ),
                        (
                            "Djupdyk 2 gånger från ytan till 1,5 m.",
                            "Deep dive twice from the surface to 1.5 m.",
                        ),
                        (
                            "Livräddning: Kunskap om punkterna 1-6 i bad- och båtvett. Kunna hjälpa en nödställd person med förlängda armen. Kast av livboj med lina från kanten till nödställd i vattnet. Utkast av livboj följt av simning med livboj 10 m.",
                            "Lifesaving: Knowledge of points 1–6 in swimming and boating safety. Be able to help a person in distress with the extended arm. Throw a lifeline buoy from the edge to a person in the water. Throw a buoy and swim 10 m towing it.",
                        ),
                    ],
                ),
            ],
        ),
        category(
            "guld",
            "Guld",
            "Gold",
            7,
            [
                badge(
                    "kandidaten",
                    "Kandidaten",
                    "The Candidate",
                    1,
                    [
                        (
                            "Simma 600 m valfritt simsätt i magläge på djupt vatten.",
                            "Swim 600 m in any stroke on your front in deep water.",
                        ),
                        (
                            "Simma 300 m i ryggläge på djupt vatten.",
                            "Swim 300 m on your back in deep water.",
                        ),
                        (
                            "Flyt 3 minuter eller 75 m.",
                            "Float for 3 minutes or 75 m.",
                        ),
                        (
                            "Flyt med kläder 2 minuter eller 50 m. Klädsel: långärmad överdel och långbyxor.",
                            "Float with clothes for 2 minutes or 50 m. Clothing: long-sleeved top and long trousers.",
                        ),
                        (
                            "Vattentramp 3 minuter eller 75 m.",
                            "Tread water for 3 minutes or 75 m.",
                        ),
                        (
                            "Längddyk 10 m.",
                            "Glide underwater for 10 m.",
                        ),
                        (
                            "Djupdyk till 2 m en gång från kanten och en gång från ytan.",
                            "Deep dive to 2 m once from the edge and once from the surface.",
                        ),
                        (
                            "Dyk från minst 1 m höjd en gång.",
                            "Dive from at least 1 m height once.",
                        ),
                        (
                            "Livräddning: 16 m simning med livboj följt av ilandföring med hjälp av livboj 16 m. Maximal tid för momentet, 2 minuter 45 sekunder. Kunskap om punkterna 1-11 i bad- och båtvett. Utkast av livboj följt av simning med livboj 25 m. Kunna hjälpa nödställd person med förlängda armen.",
                            "Lifesaving: Swim 16 m with a buoy and bring a person to shore using the buoy for 16 m. Maximum time 2 minutes 45 seconds. Knowledge of points 1–11 in swimming and boating safety. Throw a buoy and swim 25 m towing it. Be able to help a person in distress with the extended arm.",
                        ),
                    ],
                    image=False,
                ),
            ],
        ),
    ],
}


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(
        json.dumps(CATALOG, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print(f"Wrote {OUTPUT}")


if __name__ == "__main__":
    main()
