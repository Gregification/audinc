***Audinc*** is a simple java swing applicaiton, built with Maven, that hosts miscellaneous tools - of which anyone is welcome to add too. There is no restriction on the purpose of the tool, just that it is written in java and on this repo.
- because of how the classes are named the 'tools' will be refereed to as "presents"
- there is no java doc even though some classes have those properties.

## tools/presents
<br> nothing is final, many are a work in progress
- ie3301 : data set generator & limted audio sampler
- serial poke : serial port connection torubleshooter
- auto clicker : a auto clicker
- tts  : Text To Speach using the FreeTTS libary
- awake : Tells you the time that you should wake up based on sleep cycles. So that you don't interupt your REM sleep.

# How to contribute a present
recommended IDE: Eclipse 2023-09 <br>
fork & create a branch for your changes. submit a pull request when ready. 
1. make a new class inside the <code>presentables.presents</code> package that extends the <code>presentables.Presentable</code> class. the Presentable class will allow your tool to interface with the gui.
1. copy and paste everyting from the <code>presentables.presentTemplate</code> class into your newly created .java file & change the class name to the correct one.
1. add your class path to the <code>Presents</code> variable in the <code>audinc.gui.MainWin</code> class. (will eventually update to something more dynamic)
1. start typing.
