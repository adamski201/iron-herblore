# Iron Herblore
A RuneLite plugin to calculate banked herblore XP and secondary reagent deficits/surpluses for ironmen.

<img width="243" height="955" alt="image" src="https://github.com/user-attachments/assets/0379d545-21a4-44de-9102-477e978bc836" />

## Features

- **Banked xp** across your bank, potion storage, and seed vault - from seeds and coral frags to finished and upgraded potions.
- **Secondaries balance** - what your potions need against what you hold, including secondaries
  your other potions make (super attacks for super combats) and items that grind into them
  (bird nests, lava scales).
- **Gear** - prescription goggles, and the alchemist's amulet chosen per potion, with the
  charges it would use.
- **Fully configurable** - average yields per seed and coral frag, caviar success rate and lava
  shards per scale.

## How to use
Click a row's left/top side to expand the row, and show a breakdown of XP by item in the chain and recipe.
The example below shows the contribution of banked kwuarm seeds as well as grimy and clean kwuarm. Intermediates are absorbed into the seed's (or whatever the least mature item you own is) calculation.

<img width="228" height="179" alt="image" src="https://github.com/user-attachments/assets/4cd18966-7e3f-45a5-b762-c71eb06f4878" />

Click a row's right/bottom side to change the product.

<img width="235" height="256" alt="image" src="https://github.com/user-attachments/assets/86c239a2-3493-45b5-a784-7d02c047e1a2" />

## FAQ

Why not use the existing Banked Experience plugin?
- The Banked Experience plugin is designed to be generic so it can work across all skills. Given how many herblore-specific features this plugin offers, it made sense to develop a more specialised plugin.

What specific problems does this plugin solve?
- Here are the concrete reasons I set out to make this plugin:
    - To see how seeds/frags and caviar contribute to total banked XP.
    - To more accurately track the secondaries I need to gather, including e.g. bird's nest -> crushed nests.
    - To know when I can stop gathering secondaries like whiteberries and potato cacti.
    - To know when I have banked enough XP so that I can stop planting lower XP herbs like irit.
    - To see at a glance how many prayer pots, super restores, and brews I can create from my seeds.
    - To provide an enhanced UX for configuring recipes compared to existing plugins.
