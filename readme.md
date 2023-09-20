***Audinc*** is a simple java swing applicaiton that hosts miscellaneous tools - of which anyone is welcome to add too. There is no restriction on the purpose of the tool, just that it is written in java and on this repo.
- because of how the classes are named the 'tools' will be refereed to as "presents"
- there is no java doc even though some classes have those properties.

## tools/presents
<br> nothing is final, many are a work in progress
- ie3301 : data set generator that samples audio
- serial poke : a troubleshooting solution to get information about a serial port
- auto clicker : a auto clicker
- tts  : text to speach using the FreeTTS libary
  
# setup
 - use eclipse ide for java, will eventually make it fully maven when ever i or someone elsle gets to it.
1. pull this repo
1. within eclipse > file > import existing project >  select the folder that teh git project was cloned into > import
2. withineclipse > select the audinc project > prject > properties > java build path > libraries > classpath > add Jar > audinc > refrence libs > JSerial > the jar in that folder

# How to contribute
1. make a new class inside the <code>presentables.presents</code> package that extends the <code>presentables.Presentable</code> class. the Presentable class will allow your tool to interface with the gui.
1. copy and paste everyting from the <code>presentables.presentTemplate</code> class into your newly created .java file & change the class name to the correct one.
1. add your class path to the <code>Presents</code> variable in the <code>audinc.gui.MainWin</code> class.
1. start making what ever you want.
1. when pushing to the repo plz make a pull or merge request

### Trouble shooting
if your having issues with other presents try removing them from the <code>Presents</code> array in <code>audinc.gui</code> first. if that dosent work, just delete those files but dont push those changes.
