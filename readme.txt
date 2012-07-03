====================================
- Introduction ---------------------

CitizensTrader is a character plugin for Citizens 2.0 Beta.

You should be able to use it without much problem but as it is still a work in progress I cannot give any guarantees.
Use at your own risk.

- Features -------------------------

1. User friendly - you don't need to use commands to buy/sell items.
2. Admin friendly - you also don't need commands to setup available goods.
3. Customizable - NPCs can be as complex as you choose. From simple vendors to traders offering items in 
   multiple stack sizes, limits in their inventory, and advanced economy options.

- Installation ---------------------

Dependencies:

1. Citizens 2.0
2. Vault
3. economy plugin

- Commands ------------------------- 
- [] - optional () - required ------

/trader sell add id[:data] [p:price] [d:durability] [s:slot] [a:amount,next_amount,etc] [e:enchId/lvl,enchId/lvl,etc]
/trader sell remove (slot)
/trader buy add id[:data] [p:price] [s:slot] 
/trader buy remove (slot)
/trader mode simple/secure

- First steps ----------------------

To create a trader issue the following command:
/npc create "displayed name" --char trader

Alternatively, if the npc already exists you can just select it, and convert it using:
/npc character trader

By default you are in the user mode. To be able to modify the trader you have to switch to manager mode by right
clicking the npc with a stick (item id: 280)

- Using the trader (secure mode) --- (CURRENTLY NOT WORKING, USE SIMPLE MODE INSTEAD!)

1. With an empty hand right click the NPC

2. To toggle between buying/selling click the lime/cyan wool in the lower right corner.

3. 
- Clicking on available items shows the price or brings up the amount selection menu.
- Shift clicking buys the item.

4. When in the amount selection menu you can click the red wool to go back to the trader's main inventory.

5. To sell an item drag it from your inventory and click on the item in the trader's inventory.


- Using the trader (simple mode) ---

1. With an empty hand right click the NPC

2. To toggle between buying/selling click the lime/cyan wool in the lower right corner.

3. Buy menu
- Clicking once on an item shows the price or brings up the amount selection menu.
- Clicking the item a second time buys it.

4. When in the amount selection menu you can click the red wool to go back to the trader's main inventory.

5. Sell menu
- Clicking once on an item shows how much you will get for it.
- Clicking the item a second time sells it.
Note: NPCs will not buy anything unless you set up the accepted items and their prices.


- Inventory management -------------

1. Be sure to enable the manager mode first, then open the trader's inventory.

2. Options:
  
  a)  To add an item drag and drop it from your inventory
  b)  To remove an item drag and drop it to your inventory
  c)  You can move added items around to set their desired slot
  d)  To set item prices click the first white wool from the right (it should change it's color to black)
        - change in the price value is dependent on:
         1. item used 
            - wood counts as * 0.01
            - log counts as * 0.1
            - dirt counts as * 10
            - cobblestone counts as * 100
		      - items not listed above count as * 1
	      2. type of interaction
	        - right click decreases the price
           - left click increases the price
       Examples:
	    A single click with a block of drit increases the price by 10
	    Ten clicks with a block of soul sand increase the price by 10
	    One right click with 10 blocks of dirt deacreases the price by 100
	  Note:
     - you can't change the price using 'empty' cursor
	  - shift right clicking whith an 'empty' cursor changes between applying the price 
	   to the whole stack (so it becomes the final price) or a single item 
      (final price = price * number of items in the stack)
     - using AIR will show the current price
     - to exit the price editing mode click on the black wool
  e) To set up the amount selection menu shift click the item
       - using this function you can set multiple stack sizes that the player will be allowed to buy
       - when editing available sizes you are only able to add items with the same id/data as the item 
         that opens the menu 
       - set values are saved by returning to the trader's main inventory (red wool) or exiting the window
	  Note:
	  Amount selection menu won't work if you enable the stack price option
  f) To place limits on the number of offered items click the first white wool from the left
     (it should change it's color to /?/)
       - right click lowers the limit
       - left click raises the limit
       - shift right click decreases the time it takes for the limit to reset back to its original value
       - shift left click increases the time it takes for the limit to reset back to its original value
     
  
I recieved reports from users having issues with item dragging, but i'm not yet sure where the problem lies.

If you'd like to let me know of any bugs, improvements or whishes regarding the plugin you can reach me at
daniel.penkala@gmail.com

=====================================
- Thanks to -------------------------

Wreyth 	
Instinx
Gizmoholm
 - for feedback

tehbeard
 - for helping me improve my programming skills
