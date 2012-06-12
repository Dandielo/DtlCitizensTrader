====================================
- CitizensTrader -------------------

It's a plugin for the characters used by Citizens 2.0 Beta

Its still beta so use it at your own risk!

- Installing ------------------------
To make this trader working you need the last Citizens 2.0 build, Vault and an economy plugin

Creating an npc looks like this:
/npc create name --char trader

or just select the npc you want set the trader character and type
/npc character trader


To use commands you need to r.click him with a stick to enable "manager mode".


- Commands ------------------------- [] - optional ()  required
/trader sell add id[:data] [p:price] [d:durability] [s:slot] [a:amount,next_amount,etc] [e:enchId/lvl,enchId/lvl,etc]
/trader sell remove (slot)
/trader buy add id[:data] [p:price] [s:slot] 
/trader buy remove (slot)
/trader mode simple/secure



- Using the trader (secure mode) -----------------
1. r.click him to open the inventory

2. 
- click shows up the item price or opens a multiple amount choice.
- shift.click buys the item.

3. In multiple amount choice, red wool click means do back to the main trader inventory.

4. In the main trader inventory clicking on the wool cyan and lime, toggles the buy/sell list.

5. to sell an item to the trader just drag it from you'r inventory and click on the item on the trader inventory.



- Using the trader (simple mode) -----------------
1. r.click him to open the inventory

2. 
- first click shows up the item price or opens a multiple amount choice.
- a second click will buy this item

3. In multiple amount choice, red wool click means go back to the main trader inventory.

4. In the main trader inventory clicking on the wool cyan and lime, toggles the buy/sell list.

5. to sell an item click on it in you inventory to see how much money you will get.
   a second click will sell the item



- InventoryView management!!!! -------------
1. If in the management mode just open the traders inventory.

2. Options:
  a  drag items through the inventory and set their slot
  b  drag an item to the traders inventory from your inventory (adding item)
  c  drag an item from the traders inventory to your inventory (removing item)
  d  shift click an item to open his amount set section 
     - Amount set section allows to set the multiple items a player can buy
     - you can add here only materials with the same id/data 
     - it saves the amounts after leaving this section (red_woll or inventory close)
  e  click the white_wool to enter the price edit
     - r.click to decrease the price
     - l.click to increase the price
     - wood counts as * 0.01
     - log counts as * 0.1
     - dirt counts as * 10
     - cobblestone counts as * 100
     - needs to have anything in the cursor to change the price
     - AIR will show up the current price
     - click the black_wool to leave price editing
  
May have some issues with item dragging but i'm not sure.

Any bugs, improvements and whish'es report at daniel.penkala@gmail.com


P.S. sorry for my bad English
