# cas

This is 95% to make homework easier, and 5% to shame existing latex editors to up their game.

## Overview

Won't someone think of the math majors' Saturday nights?

The core idea here is that doing transformations on mathematical expressions is a huge pain.  Change one small thing to simplify and you have to write the entire thing over again.

There are of course existing computer algebra systems that can do nifty things like solving/integrating/differentiating, but the goal here is to both learn *and* complete homework efficiently.  

What's needed is a way to play around with mathematical expressions and equations, manipulate them efficiently, *and* (!!!!) take "snapshots" at certain points so you're showing your work.

The ideal would be something that:
  - parsed a simple lang (edit:  MANIPULANG!)
  - created a visible [expression tree](https://en.wikipedia.org/wiki/Binary_expression_tree) (they don't have to be binary, though), that you could manhandle a bit
  - was also continuously rendering that expression tree into latex so you didn't have to start thinking in cyclopean geometries
  - would have *limited* auto-simplifying capability---want to avoid drudgery, but gotta show your work!
  - add hotkeys to the whole thing, a la emacs.  If you've ever felt the Power of Paredit when manipulating s-expressions (which form a tree...) then you might have an idea of where this is headed.
  

  



## Setup


I've been starting with cider-jack-in-clj, then running (cljs) from user> in dev

(You'll need to run `npm install`)
