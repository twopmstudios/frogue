# fae.cljs

A simple clojurescript game engine built on [pixi.js](https://github.com/pixijs/pixi.js/).

Originally forked from https://github.com/yogthos/graviton.

## Overview

While options like [play-cljc](https://github.com/oakes/play-cljc) and [chocolatier](https://github.com/alexkehayias/chocolatier) exist and are robust options, we were seeking a lightweight & fast moving solution for cljs game development.

So here we are.

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