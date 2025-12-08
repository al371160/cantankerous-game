=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=
CIS 1200 Game Project README
PennKey: _______
=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=

===================
=: Core Concepts :=
===================

- List the four core concepts, the features they implement, and why each feature
  is an appropriate use of the concept. Incorporate the feedback you got after
  submitting your proposal.

  1. Collections
  - ArrayLists are used extensively in my game. Primarily, they are used to store the thousands
    of objects stored in the game, including bullets and squares. The properties of ArrayLists,
    like its size, searching through it for collisions, and collecting healthbars to spawn and
    objects to be removed in the tick() method.
  - ArrayLists also organizes many of my game's functions by making objects easier to access. It
    allows for easier access for object supertypes and reduces redundancies, at least to the
    standard of limiting the amount of spaghetti code I produce. Some examples are getHealth,
    death checks, and getting size of objects for constructing during game start and reset.

  2. File I/O
  - I wrote a basic read/write system to store the save data. Basically, I do health and position of every object in the
    game. I simply wrote them down in a .txt file.
  -

  3. Inheritance/Subtyping
  - This was very obvious to me and is the main feature of diep.io: its tank upgrades. The entire
    upgrade system is a huge pain to make and I will figure out the full stats later. Right now the
    only categories working is 4 and 8, which is bullet speed and movement speed. These were the
    hardest features to implement(i will explain in the physics section). The tanks themselves are
    different from regular subtyping because the tank upgrades are different in its draw functions
    and firing functions. For example, the machine gun tank has a bullet spread, the machine gun tank
    has a bullet spread system, the twin tank has a horizontal offset, and the flank guard tank has
    a flank firing system, in addition to cosmetic changes.


  4. JUnit testable component
  - I tested my upgrade system with JUnit testing.
  - I'll talk about my upgrade system: there are too many variables for my human mind to comprehend,
    objects have health, speed, and other stuff, and tanks have many upgrades. Players can upgrade
    their main stats, like movement speed and bullet speed, and bullet speed will scale with some of
    the tank classes, like sniper. I had to implement a multiplier system and I think it worked out
    pretty well. I had to do some JUnit testing to make sure everything is good. I put in 16 tests, to
    test some of the qualities of the upgrades, like multipliers transferring over with tank upgrades.

  5. Collisions (Advanced)
    The physics in this game centers around intersection, but I also created a friction value that scales
    object deceleration with the logarithm function. I made a lot of the bumping and object collision
    similar to how I think diep.io feels like. Objects accelerate quickly and decelerate slowly, and
    I imitated this effect by setting a clamp on maxSpeed. This will prove to be hard to debug as
    MovementSpeed and BulletSpeed upgrades also require maxSpeed changes.

===============================
=: File Structure Screenshot :=
===============================
- Include a screenshot of your project's file structure. This should include
  all of the files in your project, and the folders they are in. You can
  upload this screenshot in your homework submission to gradescope, named 
  "file_structure.png".

=========================
=: Your Implementation :=
=========================

- Provide an overview of each of the classes in your code, and what their
  function is in the overall game.
  * all the classes that start with ___Tank is a subtype of Tank, and all objects are children
    of GameObject, which contains variables such as health, size and death methods
  * Healthbar and UI contain methods that draw the healthbars of tanks and squares. Note that
    bullets don't have health, and that means they don't have a healthbar. They also disappear
    if the object dies or has full health, like the actual game
  * Bullet is a subtype of circle, and uses a separate "life" system, the "penetration" variable,
    that determines how much time it has before disappearing.
  * SaveData and ObjData are classes that define the objects that GameCourt uses to write its save
    files. They contain position and object type. That's about it.
  * GameCourt is where the meat is at. First, the game is drawn and objects are redrawn every tick
    based on their movements and their movements relative to the camera. This script also contains
    methods that use the upgrade system outlined in Tank, collision and damage checks, physics checks,
    and object repaints. GameCourt also contains graphics code for the various menus that the player
    has to come across.

- Were there any significant stumbling blocks while you were implementing your
  game (related to your design, or otherwise)?
  I was very confused on how the physics of the game worked. It seems like initially
  diep.io has installed some sort of java Library for smooth/spline movements of objects
  which is why you see so much smooth movement and transitions and effects. I don't want
  to code anything fancy beyond the bare basics, so I ended up abandoning the blocks "gliding,"
  smooth health decrease, smooth bullet strikes, and much more.
  I was also very confused with what I needed in each class. In my initial brainstorming, it
  helped writing what variables are there, but I had to refactor the code many times, switch
  the firing method from the main GameCourt class to individual tanks and switch all the upgrade
  variables to the tank class (which will be inherited by the higher level tanks).
  I wasn't keen on attempting the AI aspect of the game, so the game has an end state, the player
  dying from hitting too many blocks, but it is what it is at this point. Maybe in the future
  I will add more features, but now what's good is just the game works and is mostly free of
  bugs.


- Evaluate your design. Is there a good separation of functionality? How well is
  private state encapsulated? What would you refactor, if given the chance?
  I believe the private state isn't particularly well refactored: a lot of objects can be upgraded
  anywhere, and that's not a safe way to write code. However, it was the fastest. I kind of regret
  that. If I had another chance, I would methodically write my code to guard the multipliers more.
  If you see any hanging unused methods anywhere forgive me :(
  Also I basically ignored my getters and straight up just asked for some objects' variables.
  Thank you dynamic dispatch :)
  I probably also should have worried a bit more about functionality than the aesthetics. The game
  looks pretty good IMO but it needs to be worked on a lot more in order for it to be fun.


========================
=: External Resources :=
========================

- Cite any external resources (images, tutorials, etc.) that you may have used 
  while implementing your game.

  diep.io
  YouTube (how to make a main menu, how to github, subtyping)
  Lots of java docs (collections, abstract class, JUnit, Swing)
  HW 07 and HW 08 for JUnit reference and I/O
  Rajas Nanda, Chris Wang, Nicol Liu, Spencer Wang, and others for playtesting and coding advice
  (I didn't ask them for explicit code help, just thinking for my data structures)
