# Requirements
## Algemene eisen

ID  |Omschrijving|Prio |Punten|Gehaald?
----|--------------------------------------------------------------------|------|------|------
AL01|De code behoudt de packagestructuur van de aangeleverde startcode. Toegevoegde code bevindt zich in de relevante packages. |Must  |0|?
AL02|Alle code compileert en is te bouwen met Maven 3.6 of hoger, onder OpenJDK 13. Tip: controleer dit door **eerst** ```mvn clean``` uit te voeren alvorens te compileren en in te leveren. **Gebruik van Oracle versies van Java is uitdrukkelijk niet toegestaan**.   |Must  |0|?
AL03|De code is goed geformatteerd, zo nodig voorzien van commentaar, correcte variabelenamen gebruikt, bevat geen onnodig ingewikkelde constructies en is zo onderhoudbaar mogelijk opgesteld. (naar oordeel van docent)  |Must  |0|?

## Parseren

ID  |Omschrijving|Prio |Punten|Gehaald?
----|--------------------------------------------------------------------|------|------|------
PA01|Implementeer een parser plus listener die AST’s kan maken voor ICSS documenten die “eenvoudige opmaak” kan parseren, zoals beschreven in de taalbeschrijving. In `level0.icss` vind je een voorbeeld van ICSS code die je moet kunnen parseren.  `testParseLevel0()` slaagt.|Must  |10|?
PA02|Breid je grammatica en listener uit zodat nu ook assignments van variabelen en het gebruik ervan geparseerd kunnen worden. In `level1.icss` vind je voorbeeldcode die je nu zou moeten kunnen parseren. `testParseLevel1()` slaagt.|Must  |10|?
PA03|Breid je grammatica en listener uit zodat je nu ook optellen en aftrekken en vermenigvuldigen kunt parseren. In `level2.icss` vind je voorbeeld- code die je nu ook zou moeten kunnen parseren. `testParseLevel2()` slaagt.|Should|10|?
PA04|Breid je grammatica en listener uit zodat je if-statements aankunt. In `level3.icss` vind je voorbeeldcode die je nu ook zou moeten kunnen parseren. `testParseLevel3()` slaagt.|Should|10|?

## Checken

ID  |Omschrijving|Prio |Punten|Gehaald?
----|--------------------------------------------------------------------|------|------|------
CH00|Minimaal drie van onderstaande checks **moeten** zijn geïmplementeerd|Must|0|?
CH01|Controleer of er geen variabelen worden gebruikt die niet gedefinieerd zijn.|Should|	4|?
CH02|Controleer of de operanden van de operaties plus en min van gelijk type zijn en dat vermenigvuldigen enkel met scalaire waarden gebeurt. Je mag geen pixels bij percentages optellen bijvoorbeeld.|Should|4|?
CH03|Controleer of er geen kleuren worden gebruikt in operaties (plus, min en keer).|Should|2|?
CH04|Controleer of bij declaraties het type van de value klopt met de property. Declaraties zoals width: `#ff0000` of `color: 12px` zijn natuurlijk onzin.|Should|2|?
CH05|Controleer of de conditie bij een if-statement van het type boolean is (zowel bij een variabele-referentie als een boolean literal)|Should|4|?
CH06|Controleer of variabelen enkel binnen hun scope gebruikt worden|Should|4|?

## Transformeren

ID  |Omschrijving|Prio |Punten|Gehaald?
----|--------------------------------------------------------------------|------|------|------
TR01|Implementeer de `EvalExpressions` transformatie. Deze transformatie vervangt alle `Expression` knopen in de AST door een `Literal` knoop met de berekende waarde. |Should|10|?
TR02|Implementeer de `RemoveIf `transformatie. Deze transformatie verwijdert alle `IfClause`s uit de AST. Wanneer de conditie van de `IfClause` `TRUE` is wordt deze vervangen door de body van het if-statement. Als de conditie `FALSE` is dan verwijder je de `IfClause`volledig uit de AST.|Should|10|?

## Genereren

ID  |Omschrijving|Prio |Punten|Gehaald?
----|--------------------------------------------------------------------|------|------|------
GE01|Implementeer de generator in nl.han.ica.icss.generator.Generator die de AST naar een CSS2-compliant string omzet.|Must|5|?
GE02|Zorg dat de CSS met twee spaties inspringing per scopeniveau  gegenereerd wordt.|Must|5|?
