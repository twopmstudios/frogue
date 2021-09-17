# 🐸 FROGUE

[![Twitter](https://img.shields.io/twitter/follow/vivavolt?label=%40vivavolt&style=flat&colorA=000000&colorB=000000&logo=twitter&logoColor=000000)](https://twitter.com/vivavolt)
[![Donate (ETH)](https://img.shields.io/badge/Donate-(ETH)-f5f5f5?style=flat&colorA=000000&colorB=000000)](https://blockchain.com/eth/address/0x981e493b795A7a28c43Bf8d7a8E125C419435Fa7)
[![Donate ($)](https://img.shields.io/badge/Donate-($)-f5f5f5?style=flat&colorA=000000&colorB=000000)](https://ko-fi.com/vivavolt)
![Language](https://img.shields.io/github/languages/top/twopmstudios/frogue?style=flat&colorA=000000&colorB=000000)
![License](https://img.shields.io/github/license/twopmstudios/frogue?style=flat&colorA=000000&colorB=000000)

![image](https://user-images.githubusercontent.com/5009316/110710156-1a46c080-8249-11eb-8a0b-cccc99060384.png)
![image](https://user-images.githubusercontent.com/5009316/110710182-2763af80-8249-11eb-8a7f-7aa8a0809279.png)

A simple clojurescript roguelike game & engine built on [pixi.js](https://github.com/pixijs/pixi.js/).

Originally forked from https://github.com/yogthos/graviton.

You be a frogge. Eat gnats and mosquitos but be afeared for he be hurting your eggs. Findeth another frogge then find a spotte to lay your eggs.

### Instructions

Arrows to move and attack. 
WASD to lick enemies.
J to enter JUMP mode, Arrows to choose jump direction.

### Credits

Programming, SFX & Art - Ben Follington ([@vivavolt](https://twitter.com/vivavolt))
Design & Writing - Ricky James ([@iammonshushu](https://twitter.com/iammonshushu))
Music - AJ Booker ([@ajbookr](https://twitter.com/ajbookr))

SFX Sample Credit
https://freesound.org/people/InspectorJ/sounds/339677/

## Overview

While [play-cljc](https://github.com/oakes/play-cljc) and [chocolatier](https://github.com/alexkehayias/chocolatier) exist and are robust options, we were seeking a lightweight & fast moving solution for cljs game development.

So here we are, we'll see if this engine gets used again.

## Development

To get an interactive development environment run:

    lein fig:build

This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

	lein clean

To create a production build run:

	lein clean
	lein fig:min


## License

Copyright © 2021 TwoPM Studios
