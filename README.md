# cas

This is 95% to make homework easier, and 5% to shame existing latex editors to up their game.

## Overview

Won't someone think of the students?

The core idea here is that doing transformations on mathematical expressions is a huge pain.  Change one small thing to simplify and you have to write the entire thing over again.

There are of course existing computer algebra systems that can do nifty things like solving/integrating/differentiating, but the goal here is to both learn *and* complete homework efficiently.  

What's needed is a way to play around with mathematical expressions and equations, manipulate them, *and* (!!!!) take "snapshots" at certain points so you're showing your work.

The ideal would be something that:
  - parsed a simple lang
  - created a visible [expression tree](https://en.wikipedia.org/wiki/Binary_expression_tree) (they don't have to be binary, thouhg), that you could manhandle a bit
  - was also continuously rendering that expression tree into latex so you didn't have to start thinking in cyclopean geometries
  - would have *limited* auto-simplifying capability---want to avoid drudgery, but gotta show your work!
  - add hotkeys to the whole thing, a la emacs.  If you've ever felt the Power of Paredit when manipulating s-expressions (which form a tree...) then you might have an idea of where this is headed.
  
  Unfortunately, my particular use case right now is a differential equations class, and while there's loads of cas's that can do symbolic differentiation, the only one I can find that gives easy access to the expression tree ([mathjs](https://mathjs.org/)) thinks of calc operations as something you do *to* an expression, rather than something that's *part* of an expression.
  
So there's no visible tree yet.  Basically you can write latex snippets and modify them, while having them render instantly accordingly.  See something you like, and you can click a button (should be a hotkey eventually) and send them off to the "homework" field.  Yes, this is specifically to make homework easier.  Eye on the ball!


## Setup

This is very much in dev so the only way to run this right now is with a local figwheel server.  But it's not intended for this to be "server-bound"--- I'd like to package this whole thing up at some point.

But for now, you'll need [leiningen](https://github.com/technomancy/leiningen).  Clone the repo, and run 

    lein deps
    
from within it, then

    lein figwheel
    
And away you go!

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
