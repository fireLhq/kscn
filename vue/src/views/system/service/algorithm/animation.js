// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco


function installEvents(runtime) {
function EventListener()
{
	this.events = [];
}


EventListener.prototype.removeListener = function(kind, scope, func)
{
	if (this.events[kind] == undefined)
	{
		return;
	}
	var scopeFunctions = null;
	var i;
	for (i = 0; i < this.events[kind].length; i++)
	{
		if (this.events[kind][i].scope == scope)
		{
			scopeFunctions = this.events[kind][i];
			break;
		}
	}
	if (scopeFunctions == null)
	{
		return;
	}
	for (i = 0; i < scopeFunctions.functions.length; i++)
	{
		if (scopeFunctions.functions[i] == func)
		{
			scopeFunctions.functions.splice(i,1);
			return;
		}
	}
}


EventListener.prototype.addListener = function(kind, scope, func)
{
	if (this.events[kind] === undefined)
	{
		this.events[kind] = [];
	}
	var i;
	var scopeFunctions = null;
	for (i = 0; i < this.events[kind].length; i++)
	{
		if (this.events[kind][i].scope == scope)
		{
			scopeFunctions = this.events[kind][i];
			break;
		}
	}
	if (scopeFunctions === null)
	{
		this.events[kind].push({scope:scope, functions:[] });
		scopeFunctions = this.events[kind][this.events[kind].length - 1];
	}
	for (i = 0; i < scopeFunctions.functions.length; i++)
	{
		if (scopeFunctions.functions[i] == func)
		{
			return;
		}
	}
	scopeFunctions.functions.push(func);
}

EventListener.prototype.fireEvent = function(kind, event)
{
	// TODO:  Should add a deep clone here ...
	if (this.events[kind] !== undefined)
	{
		for (var i = 0; i < this.events[kind].length; i++)
		{
			var objects = this.events[kind][i];
			var functs = objects.functions;
			var scope = objects.scope
			for (var j = 0; j <functs.length; j++)
			{
				var func = functs[j];
				func.call(scope,event);
			}
		}
	}

}


/*
function Source()
{

}

Source.prototype = new EventListener();
Source.prototype.constructor = Source;
Source.prototype.testFire = function()
{
	this.fireEvent("test","testcontents");
	this.fireEvent("test2","test2contents");
}



function Client(eventSource)
{

	this.first = function(blah)
	{
		alert("first:" + blah);
	}

	this.second = function(blah)
	{
		alert("second:" + blah);
	}
	eventSource.addListener("test", this, this.first);
	eventSource.addListener("test", this, this.first);
	eventSource.addListener("test", this, this.second);
	eventSource.removeListener("test", this, this.first);


}


function init()
{
	var src = new Source;
	var c = new Client(src);
	src.testFire();
}

*/


Object.defineProperties(runtime, {
"EventListener": { configurable: true, get: function() { return EventListener; }, set: function(value) { EventListener = value; } }
});
}


// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco


// Base "class": UndoBlock

function installUndo(runtime) {
function UndoBlock()
{

}

UndoBlock.prototype.addUndoAnimation = function(animationList)
{
	return false;
}

UndoBlock.prototype.undoInitialStep = function(world)
{

}

////////////////////////////////////////////////////////////
// UndoMove
////////////////////////////////////////////////////////////

function UndoMove(id, fmX, fmy, tx, ty)
{
	this.objectID = id;
	this.fromX = fmX;
	this.fromY = fmy;
	this.toX = tx;
	this.toY = ty;
}


UndoMove.prototype = new UndoBlock();
UndoMove.prototype.constructor = UndoMove;

UndoMove.prototype.addUndoAnimation = function (animationList)
{
	var nextAnim = new runtime.SingleAnimation(this.objectID, this.fromX, this.fromY, this.toX, this.toY);
	animationList.push(nextAnim);
	return true;
}

////////////////////////////////////////////////////////////
// UndoCreate
////////////////////////////////////////////////////////////

function UndoCreate(id)
{
	this.objectID = id;
}

UndoCreate.prototype = new UndoBlock();
UndoCreate.prototype.constructor = UndoCreate;


UndoCreate.prototype.undoInitialStep = function(world)
{
			world.removeObject(this.objectID);
}

////////////////////////////////////////////////////////////
// UndoHighlight
////////////////////////////////////////////////////////////

function UndoHighlight(id, val)
{
	this.objectID = id;
	this.highlightValue = val;
}

UndoHighlight.prototype = new UndoBlock();
UndoHighlight.prototype.constructor = UndoHighlight;

UndoHighlight.prototype.undoInitialStep = function(world)
{
	world.setHighlight(this.objectID, this.highlightValue);
}


////////////////////////////////////////////////////////////
// UndoSetHeight
////////////////////////////////////////////////////////////

function UndoSetHeight(id, val)
{
	this.objectID = id;
	this.height = val;
}

UndoSetHeight.prototype = new UndoBlock();
UndoSetHeight.prototype.constructor = UndoSetHeight;

UndoSetHeight.prototype.undoInitialStep = function(world)
{
	world.setHeight(this.objectID, this.height);
}

////////////////////////////////////////////////////////////
// UndoSetWidth
////////////////////////////////////////////////////////////

function UndoSetWidth(id, val)
{
	this.objectID = id;
	this.width = val;
}

UndoSetWidth.prototype = new UndoBlock();
UndoSetWidth.prototype.constructor = UndoSetWidth;

UndoSetWidth.prototype.undoInitialStep = function(world)
{
	world.setWidth(this.objectID, this.width);
}


////////////////////////////////////////////////////////////
// UndoSetNumElements
////////////////////////////////////////////////////////////
function UndoSetNumElements(obj, newNumElems)
{
	this.objectID = obj.objectID;
	this.sizeBeforeChange = obj.getNumElements();
	this.sizeAfterChange = newNumElems;
	if (this.sizeBeforeChange > this.sizeAfterChange)
	{
		this.labels = new Array(this.sizeBeforeChange - this.sizeAfterChange);
		this.colors = new Array(this.sizeBeforeChange - this.sizeAfterChange);
		for (var i = 0; i < this.sizeBeforeChange - this.sizeAfterChange; i++)
		{
			this.labels[i] = obj.getText(i+this.sizeAfterChange);
			this.colors[i] = obj.getTextColor(i+this.sizeAfterChange);
		}

	}
}

UndoSetNumElements.prototype = new UndoBlock();
UndoSetNumElements.prototype.constructor = UndoSetNumElements;


UndoSetNumElements.prototype.undoInitialStep = function(world)
{
	world.setNumElements(this.objectID, this.sizeBeforeChange);
	if (this.sizeBeforeChange > this.sizeAfterChange)
	{
		for (var i = 0; i < this.sizeBeforeChange - this.sizeAfterChange; i++)
		{
			world.setText(this.objectID, this.labels[i], i+this.sizeAfterChange);
			world.setTextColor(this.objectID, this.colors[i], i+this.sizeAfterChange);
		}
	}
}


////////////////////////////////////////////////////////////
// UndoSetAlpha
////////////////////////////////////////////////////////////

function UndoSetAlpha(id, alph)
{
	this.objectID = id;
	this.alphaVal = alph;
}

UndoSetAlpha.prototype = new UndoBlock();
UndoSetAlpha.prototype.constructor = UndoSetAlpha;

UndoSetAlpha.prototype.undoInitialStep = function(world)
{
	world.setAlpha(this.objectID, this.alphaVal);
}

////////////////////////////////////////////////////////////
// UndoSetNull
////////////////////////////////////////////////////////////

function UndoSetNull(id, nv)
{
	this.objectID = id;
	this.nullVal = nv;
}

UndoSetNull.prototype = new UndoBlock();
UndoSetNull.prototype.constructor = UndoSetNull;

UndoSetNull.prototype.undoInitialStep = function(world)
{
	world.setNull(this.objectID, this.nullVal);
}

////////////////////////////////////////////////////////////
// UndoSetForegroundColor
////////////////////////////////////////////////////////////

function UndoSetForegroundColor(id, color)
{
	this.objectID = id;
	this.color = color;
}

UndoSetForegroundColor.prototype = new UndoBlock();
UndoSetForegroundColor.prototype.constructor = UndoSetForegroundColor;

UndoSetForegroundColor.prototype.undoInitialStep =  function (world)
{
	world.setForegroundColor(this.objectID, this.color);
}

////////////////////////////////////////////////////////////
// UndoSetBackgroundColor
////////////////////////////////////////////////////////////

function UndoSetBackgroundColor(id, color)
{
	this.objectID = id;
	this.color = color;
}

UndoSetBackgroundColor.prototype = new UndoBlock();
UndoSetBackgroundColor.prototype.constructor = UndoSetBackgroundColor;

UndoSetBackgroundColor.prototype.undoInitialStep =  function (world)
{
	world.setBackgroundColor(this.objectID, this.color);
}



////////////////////////////////////////////////////////////
// UndoSetHighlightIndex
////////////////////////////////////////////////////////////

function UndoSetHighlightIndex(id, index)
{
	this.objectID = id;
	this.index = index;
}

UndoSetHighlightIndex.prototype = new UndoBlock();
UndoSetHighlightIndex.prototype.constructor = UndoSetHighlightIndex;

UndoSetHighlightIndex.prototype.undoInitialStep =  function (world)
{
	world.setHighlightIndex(this.objectID, this.index);
}



////////////////////////////////////////////////////////////
// UndoSetText
////////////////////////////////////////////////////////////



function UndoSetText(id, str, index)
{
	this.objectID = id;
	this.newText = str;
	this.labelIndex = index;
}

UndoSetText.prototype = new UndoBlock();
UndoSetText.prototype.constructor = UndoSetText;

UndoSetText.prototype.undoInitialStep = function(world)
{
	world.setText(this.objectID, this.newText, this.labelIndex);
}
////////////////////////////////////////////////////////////
// UndoSetTextColor
////////////////////////////////////////////////////////////



function UndoSetTextColor(id, color, index)
{
	this.objectID = id;
	this.color = color;
	this.index = index;
}

UndoSetTextColor.prototype = new UndoBlock();
UndoSetTextColor.prototype.constructor = UndoSetTextColor;

UndoSetTextColor.prototype.undoInitialStep = function(world)
{
	world.setTextColor(this.objectID, this.color, this.index);
}



////////////////////////////////////////////////////////////
// UndoHighlightEdge
////////////////////////////////////////////////////////////

function UndoHighlightEdge(from, to, val)
{
	this.fromID = from;
	this.toID = to;
	this.highlightValue = val;
}

UndoHighlightEdge.prototype = new UndoBlock();
UndoHighlightEdge.prototype.constructor = UndoHighlightEdge;

UndoHighlightEdge.prototype.undoInitialStep = function(world)
{
	world.setEdgeHighlight(this.fromID, this.toID, this.highlightValue);
}


////////////////////////////////////////////////////////////
// UndoSetEdgeColor
////////////////////////////////////////////////////////////

function UndoSetEdgeColor(from, to, oldColor)
{
	this.fromID = from;
	this.toID = to;
	this.color = oldColor;
}

UndoSetEdgeColor.prototype = new UndoBlock();
UndoSetEdgeColor.prototype.constructor = UndoSetEdgeColor;

UndoSetEdgeColor.prototype.undoInitialStep = function(world)
{
	world.setEdgeColor(this.fromID, this.toID, this.color);
}


////////////////////////////////////////////////////////////
// UndoSetEdgeAlpha
////////////////////////////////////////////////////////////

function UndoSetEdgeAlpha(from, to, oldAplha)
{
	this.fromID = from;
	this.toID = to;
	this.alpha  = oldAplha;
}

UndoSetEdgeAlpha.prototype = new UndoBlock();
UndoSetEdgeAlpha.prototype.constructor = UndoSetEdgeAlpha;

UndoSetEdgeAlpha.prototype.undoInitialStep = function(world)
{
	world.setEdgeAlpha(this.fromID, this.toID, this.alpha);
}

////////////////////////////////////////////////////////////
// UndoSetPosition
////////////////////////////////////////////////////////////

function UndoSetPosition(id, x, y)
{
	this.objectID = id;
	this.x = x;
	this.y = y;
}

UndoSetPosition.prototype = new UndoBlock();
UndoSetPosition.prototype.constructor = UndoSetPosition;


UndoSetPosition.prototype.undoInitialStep = function(world)
{
	world.setNodePosition(this.objectID, this.x, this.y);
}



Object.defineProperties(runtime, {
"UndoBlock": { configurable: true, get: function() { return UndoBlock; }, set: function(value) { UndoBlock = value; } },
"UndoMove": { configurable: true, get: function() { return UndoMove; }, set: function(value) { UndoMove = value; } },
"UndoCreate": { configurable: true, get: function() { return UndoCreate; }, set: function(value) { UndoCreate = value; } },
"UndoHighlight": { configurable: true, get: function() { return UndoHighlight; }, set: function(value) { UndoHighlight = value; } },
"UndoSetHeight": { configurable: true, get: function() { return UndoSetHeight; }, set: function(value) { UndoSetHeight = value; } },
"UndoSetWidth": { configurable: true, get: function() { return UndoSetWidth; }, set: function(value) { UndoSetWidth = value; } },
"UndoSetNumElements": { configurable: true, get: function() { return UndoSetNumElements; }, set: function(value) { UndoSetNumElements = value; } },
"UndoSetAlpha": { configurable: true, get: function() { return UndoSetAlpha; }, set: function(value) { UndoSetAlpha = value; } },
"UndoSetNull": { configurable: true, get: function() { return UndoSetNull; }, set: function(value) { UndoSetNull = value; } },
"UndoSetForegroundColor": { configurable: true, get: function() { return UndoSetForegroundColor; }, set: function(value) { UndoSetForegroundColor = value; } },
"UndoSetBackgroundColor": { configurable: true, get: function() { return UndoSetBackgroundColor; }, set: function(value) { UndoSetBackgroundColor = value; } },
"UndoSetHighlightIndex": { configurable: true, get: function() { return UndoSetHighlightIndex; }, set: function(value) { UndoSetHighlightIndex = value; } },
"UndoSetText": { configurable: true, get: function() { return UndoSetText; }, set: function(value) { UndoSetText = value; } },
"UndoSetTextColor": { configurable: true, get: function() { return UndoSetTextColor; }, set: function(value) { UndoSetTextColor = value; } },
"UndoHighlightEdge": { configurable: true, get: function() { return UndoHighlightEdge; }, set: function(value) { UndoHighlightEdge = value; } },
"UndoSetEdgeColor": { configurable: true, get: function() { return UndoSetEdgeColor; }, set: function(value) { UndoSetEdgeColor = value; } },
"UndoSetEdgeAlpha": { configurable: true, get: function() { return UndoSetEdgeAlpha; }, set: function(value) { UndoSetEdgeAlpha = value; } },
"UndoSetPosition": { configurable: true, get: function() { return UndoSetPosition; }, set: function(value) { UndoSetPosition = value; } }
});
}


// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco

function installObject(runtime) {
function AnimatedObject()
{
	this.init();
}

AnimatedObject.prototype.init  = function()
{
	this.backgroundColor = "#FFFFFF";
	this.foregroundColor = "#000000";
	this.highlighted = false;
	this.objectID = -1;
	this.layer = 0;
	this.addedToScene = true;
	this.label = "";
	this.labelColor = "#000000";
	this.alpha = 1.0;
	this.x = 0;
	this.y = 0;
	this.minHeightDiff = 3;
	this.range = 5;
        this.highlightIndex = -1;
        this.highlightIndexDirty = true;
}

AnimatedObject.prototype.alwaysOnTop = false;

AnimatedObject.prototype.setBackgroundColor = function(newColor)
{
	this.backgroundColor = newColor;
}

AnimatedObject.prototype.setForegroundColor = function(newColor)
{
	this.foregroundColor = newColor;
}

AnimatedObject.prototype.setNull = function()
{

}

AnimatedObject.prototype.getNull = function()
{
	return false;
}

AnimatedObject.prototype.setAlpha = function(newAlpha)
{
	this.alpha = newAlpha;
}

AnimatedObject.prototype.getAlpha = function()
{
	return this.alpha;
}

AnimatedObject.prototype.setForegroundColor = function(newColor)
{
	this.foregroundColor = newColor;
	this.labelColor = newColor;
}


AnimatedObject.prototype.getHighlight = function()
{
	return this.highlighted;
}

AnimatedObject.prototype.getWidth = function()
{
	// TODO:  Do we want to throw here?  Should always override this ...
	return 0;
}

AnimatedObject.prototype.getHeight = function()
{
	// TODO:  Do we want to throw here?  Should always override this ...
	return 0;
}

AnimatedObject.prototype.setHighlight = function(value)
{
	this.highlighted = value;
}

AnimatedObject.prototype.centerX = function()
{
	return this.x;
}

AnimatedObject.prototype.setWidth = function(newWidth)
{
	// TODO:  Do we want to throw here?  Should always override this ...
}



AnimatedObject.prototype.centerY = function()
{
	return this.y;
}


AnimatedObject.prototype.getAlignLeftPos = function(otherObject)
{
    return [otherObject.right()+ this.getWidth() / 2, otherObject.centerY()];
}

AnimatedObject.prototype.getAlignRightPos = function(otherObject)
{

    return [otherObject.left() - this.getWidth() / 2, otherObject.centerY()];
}

AnimatedObject.prototype.getAlignTopPos = function(otherObject)
{

    return [otherObject.centerX(), otherObject.top() - this.getHeight() / 2];
}
AnimatedObject.prototype.getAlignBottomPos = function(otherObject)
{
    return [otherObject.centerX(), otherObject.bottom() + this.getHeight() / 2];
}


AnimatedObject.prototype.alignLeft = function(otherObject)
{
	// Assuming centering.  Overridden method could modify if not centered
	//  (See AnimatedLabel, for instance)
	this.y = otherObject.centerY();
	this.x = otherObject.right() + this.getWidth() / 2;
}

AnimatedObject.prototype.alignRight = function(otherObject)
{
	// Assuming centering.  Overridden method could modify if not centered
	//  (See AnimatedLabel, for instance)
	this.y = otherObject.centerY();
	this.x = otherObject.left() - this.getWidth() / 2;
}


AnimatedObject.prototype.alignTop = function(otherObject)
{
	// Assuming centering.  Overridden method could modify if not centered

	this.x = otherObject.centerX();
	this.y = otherObject.top() - this.getHeight() / 2;


}


AnimatedObject.prototype.alignBottom = function(otherObject)
{
	this.x = otherObject.centerX();
	this.y = otherObject.bottom() + this.getHeight() / 2;

}



/* TODO:  Do we need these in the base?
		function left(): Number
		{
			return x - getWidth() / 2;
		}

		function right():Number
		{
			return x + getWidth() / 2;
		}

		function top():Number
		{
			return y - getHeight() / 2;
		}

		function bottom():Number
		{
			return y + getHeight() / 2;
		}

		function centerX():Number
		{
			return x;
		}

		function centerY():Number
		{
			return y;
		}
		*/


AnimatedObject.prototype.getClosestCardinalPoint = function(fromX, fromY)
{
	var xDelta;
	var yDelta;
	var xPos;
	var yPos;

	if (fromX < this.left())
	{
		xDelta = this.left() - fromX;
		xPos = this.left();
 	}
	else if (fromX > this.right())
	{
		xDelta = fromX - this.right();
		xPos = this.right();
    }
	else
	{
		xDelta = 0;
		xPos = this.centerX();
	}

	if (fromY < this.top())
	{
		yDelta = this.top() - fromY;
		yPos = this.top();
	}
	else if (fromY > this.bottom())
	{
		yDelta = fromY - this.bottom();
		yPos = this.bottom();
    }
	else
	{
		yDelta = 0;
		yPos = this.centerY();
	}

	if (yDelta > xDelta)
	{
		xPos = this.centerX();
	}
	else
	{
		yPos  = this.centerY();
	}

	return [xPos, yPos];
}


AnimatedObject.prototype.centered = function()
{
	return false;
}


AnimatedObject.prototype.pulseHighlight = function(frameNum)
{
	if (this.highlighted)
	{
				var frameMod = frameNum / 7.0;
				var delta  = Math.abs((frameMod) % (2 * this.range  - 2) - this.range + 1)
				this.highlightDiff =  delta + this.minHeightDiff;
	}

}

AnimatedObject.prototype.getTailPointerAttachPos = function(fromX, fromY, anchorPoint)
{
	return [this.x, this.y];
}


AnimatedObject.prototype.getHeadPointerAttachPos = function(fromX, fromY)
{
	return [this.x, this.y];
}

/*public function createUndoDelete() : UndoBlock
{
			// Must be overriden!
			return null;
}
*/
AnimatedObject.prototype.identifier = function()
{
	return this.objectID;
}

AnimatedObject.prototype.getText = function(index)
{
	return this.label;
}

AnimatedObject.prototype.getTextColor = function(textIndex)
{
	return this.labelColor
}

AnimatedObject.prototype.setTextColor = function(color, textIndex)
{
		this.labelColor = color;
}

AnimatedObject.prototype.setText = function(newText, textIndex)
{
	this.label = newText;
}

AnimatedObject.prototype.setHighlightIndex = function(hlIndex)
{
   this.highlightIndex = hlIndex;
   this.highlightIndexDirty = true;
}


AnimatedObject.prototype.getHighlightIndex = function()
{
   return this.highlightIndex;
}

Object.defineProperties(runtime, {
"AnimatedObject": { configurable: true, get: function() { return AnimatedObject; }, set: function(value) { AnimatedObject = value; } }
});
}


﻿// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco

function installLabel(runtime) {
var strList;
function AnimatedLabel(id, val, center, initialWidth)
{
	this.centering = center;
	this.label = val;
	this.highlighted = false;
	this.objectID = id;
	this.alpha = 1.0;
	this.addedToScene = true;
	this.labelColor = "#000000";
	this.textWidth = 0;
	if (initialWidth != undefined)
	{
		this.textWidth = initialWidth;
	}

        this.leftWidth = -1;
        this.centerWidth = -1;
        this.highlightIndex = -1;
}

AnimatedLabel.prototype = new runtime.AnimatedObject();
AnimatedLabel.prototype.constructor = AnimatedLabel;

AnimatedLabel.prototype.alwaysOnTop = true;


AnimatedLabel.prototype.centered = function()
{
	return this.centering;
}


AnimatedLabel.prototype.draw = function(ctx)
{
	if (!this.addedToScene)
	{
		return;
	}

	ctx.globalAlpha = this.alpha;
	ctx.font = '10px sans-serif';

        var startingXForHighlight = this.x;

        if (this.highlightIndex >= this.label.length)
        {
             this.highlightIndex = -1;
        }
        if (this.highlightIndexDirty && this.highlightIndex != -1)
        {
              this.leftWidth = ctx.measureText(this.label.substring(0,this.highlightIndex)).width;
              this.centerWidth = ctx.measureText(this.label.substring(this.highlightIndex, this.highlightIndex+1)).width;
	      this.highlightIndexDirty = false;
        }

	if (this.centering)
	{
                if (this.highlightIndex != -1)
                {
		    startingXForHighlight = this.x - this.width / 2;
                    ctx.textAlign = 'left';
                    ctx.textBaseline   = 'middle';
                }
                else
                {
      		    ctx.textAlign = 'center';
                    ctx.textBaseline   = 'middle';
                }
	}
	else
	{
		ctx.textAlign = 'left';
		ctx.textBaseline   = 'top';
	}
	if (this.highlighted)
	{
	    ctx.strokeStyle = "#ffaaaa";
	    ctx.fillStyle = "#ff0000";
		ctx.lineWidth = this.highlightDiff;
		ctx.strokeText(this.label, this.x, this.y);
		//ctx.fillText(this.label, this.x, this.y);
	}
	ctx.strokeStyle = this.labelColor;
	ctx.fillStyle = this.labelColor;
	ctx.lineWidth = 1;
	strList = this.label.split("\n");
	if (strList.length == 1)
	{
                if (this.highlightIndex == -1)
                {
                    ctx.fillText(this.label, this.x, this.y);
                }
                else
                {
                    var leftStr = this.label.substring(0, this.highlightIndex);
                    var highlightStr = this.label.substring(this.highlightIndex, this.highlightIndex + 1)
                    var rightStr = this.label.substring(this.highlightIndex + 1)
                    ctx.fillText(leftStr, startingXForHighlight, this.y)
 	            ctx.strokeStyle = "#FF0000";
	            ctx.fillStyle = "#FF0000";
                    ctx.fillText(highlightStr, startingXForHighlight + this.leftWidth, this.y)


	            ctx.strokeStyle = this.labelColor;
	            ctx.fillStyle = this.labelColor;
                    ctx.fillText(rightStr, startingXForHighlight + this.leftWidth + this.centerWidth, this.y)


                }
		//this.textWidth = ctx.measureText(this.label).width;
	}
	else
	{
		var offset = (this.centering)?  (1.0 - strList.length) / 2.0 : 0;
		for (var i = 0; i < strList.length; i++)
		{
			ctx.fillText(strList[i], this.x, this.y + offset + i * 12);
			//this.textWidth = Math.max(this.textWidth, ctx.measureText(strList[i]).width);
		}
	}
	ctx.closePath();
}


AnimatedLabel.prototype.getAlignLeftPos = function(otherObject)
{
    if (this.centering)
    {
	return [otherObject.left() - this.textWidth / 2, this.y = otherObject.centerY()];
    }
    else
    {
	return [otherObject.left() - this.textWidth, otherObject.centerY() - 5];
    }
}

AnimatedLabel.prototype.alignLeft = function(otherObject)
{
	if (this.centering)
	{
		this.y = otherObject.centerY();
		this.x = otherObject.left() - this.textWidth / 2;
	}
	else
	{
		this.y = otherObject.centerY() - 5;
		this.x = otherObject.left() - this.textWidth;
	}
}

AnimatedLabel.prototype.alignRight = function(otherObject)
{
	if (this.centering)
	{
		this.y = otherObject.centerY();
		this.x = otherObject.right() + this.textWidth / 2;
	}
	else
	{
		this.y = otherObject.centerY() - 5;
		this.x = otherObject.right();
	}
}
AnimatedLabel.prototype.getAlignRightPos = function(otherObject)
{
    if (this.centering)
    {
	return [otherObject.right() + this.textWidth / 2, otherObject.centerY()];
    }
    else
    {
	return [otherObject.right(), otherObject.centerY() - 5];
    }
}


AnimatedLabel.prototype.alignTop = function(otherObject)
{
	if (this.centering)
	{
		this.y = otherObject.top() - 5;
		this.x = otherObject.centerX();
	}
	else
	{
		this.y = otherObject.top() - 10;
		this.x = otherObject.centerX() -this.textWidth / 2;
	}
}


AnimatedLabel.prototype.getAlignTopPos = function(otherObject)
{
	if (this.centering)
	{
		return [otherObject.centerX(), otherObject.top() - 5];
	}
	else
	{
	    return [otherObject.centerX() -this.textWidth / 2, otherObject.top() - 10];
	}
}


AnimatedLabel.prototype.alignBottom = function(otherObject)
{
	if (this.centering)
	{
		this.y = otherObject.bottom() + 5;
		this.x = otherObject.centerX();
	}
	else
	{
		this.y = otherObject.bottom();
		this.x = otherObject.centerX() - this.textWidth / 2;
	}
}


AnimatedLabel.prototype.getAlignBottomPos = function(otherObject)
{
	if (this.centering)
	{
	    return [otherObject.centerX(),  otherObject.bottom() + 5];
	}
	else
	{
	    return [otherObject.centerX() - this.textWidth / 2,  otherObject.bottom()];
	}
}



AnimatedLabel.prototype.getWidth = function()
{
	return this.textWidth;
}

AnimatedLabel.prototype.getHeight = function()
{
	return 10;  // HACK!  HACK!  HACK!  HACK!
}


AnimatedLabel.prototype.setHighlight = function(value)
{
	this.highlighted = value;
}

AnimatedLabel.prototype.createUndoDelete = function()
{
	return new UndoDeleteLabel(this.objectID, this.label, this.x, this.y, this.centering, this.labelColor, this.layer, this.highlightIndex);
}


AnimatedLabel.prototype.centerX = function()
{
	if (this.centering)
	{
		return this.x;
	}
	else
	{
		return this.x + this.textWidth;
	}

}

AnimatedLabel.prototype.centerY = function()
{
	if (this.centering)
	{
		return this.y;
	}
	else
	{
		return this.y + 5; //
	}

}

AnimatedLabel.prototype.top = function()
{
	   if (this.centering)
	   {
		   return  this.y - 5; //TODO: Un-Hardwire
	   }
	   else
	   {
			return this.y;
	   }
}


AnimatedLabel.prototype.bottom = function()
{
   if (this.centering)
   {
	   return  this.y + 5; // TODO: + height / 2;
   }
   else
   {
	   return  this.y + 10; // TODO: + hieght;
   }
}


AnimatedLabel.prototype.right = function()
{
   if (this.centering)
   {
	   return  this.x + this.textWidth / 2; // TODO: + width / 2;
   }
   else
   {
	   return  this.x + this.textWidth; // TODO: + width;
   }
}


AnimatedLabel.prototype.left = function()
{
   if (this.centering)
   {
	   return this. x - this.textWidth / 2;
   }
   else
   {
	   return  this.x; // TODO:  - a little?
   }
}


AnimatedLabel.prototype.setHighlightIndex = function(hlIndex)
{
    // Only allow highlight index for labels that don't have End-Of-Line
    if (this.label.indexOf("\n") == -1 && this.label.length > hlIndex)
    {
         this.highlightIndex = hlIndex;
         this.highlightIndexDirty = true;
    }
    else
    {
         this.highlightIndex = -1;

    }
}


 AnimatedLabel.prototype.getTailPointerAttachPos = function(fromX, fromY, anchorPoint)
 {
	return this.getClosestCardinalPoint(fromX, fromY);
 }

AnimatedLabel.prototype.getHeadPointerAttachPos = function (fromX, fromY)
{
	return this.getClosestCardinalPoint(fromX, fromY);
}

AnimatedLabel.prototype.setText = function(newText, textIndex, initialWidth)
{
	this.label = newText;
	if (initialWidth != undefined)
	{
		this.textWidth = initialWidth;
	}
}



function UndoDeleteLabel(id, lab, x, y, centered, color, l, hli)
{
	this.objectID = id;
	this.posX = x;
	this.posY = y;
	this.nodeLabel = lab;
	this.labCentered = centered;
	this.labelColor = color;
	this.layer = l;
        this.highlightIndex = hli;
        this.dirty = true;
}

UndoDeleteLabel.prototype = new runtime.UndoBlock();
UndoDeleteLabel.prototype.constructor = UndoDeleteLabel;

UndoDeleteLabel.prototype.undoInitialStep = function(world)
{
	world.addLabelObject(this.objectID, this.nodeLabel, this.labCentered);
	world.setNodePosition(this.objectID, this.posX, this.posY);
	world.setForegroundColor(this.objectID, this.labelColor);
	world.setLayer(this.objectID, this.layer);
}


Object.defineProperties(runtime, {
"AnimatedLabel": { configurable: true, get: function() { return AnimatedLabel; }, set: function(value) { AnimatedLabel = value; } },
"UndoDeleteLabel": { configurable: true, get: function() { return UndoDeleteLabel; }, set: function(value) { UndoDeleteLabel = value; } }
});
}


// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco


function installCircle(runtime) {
var AnimatedCircle = function(objectID, objectLabel)
{
	this.objectID = objectID;
	this.label = objectLabel;
	this.radius = 20;
	this.thickness = 3;
	this.x = 0;
	this.y = 0;
	this.alpha = 1.0;
	this.addedToScene = true;
        this.highlightIndex = -1;
/*	this.foregroundColor  = '#007700';
	this.backgroundColor  = '#EEFFEE';
 */
}

AnimatedCircle.prototype = new runtime.AnimatedObject();
AnimatedCircle.prototype.constructor = AnimatedCircle;

AnimatedCircle.prototype.getTailPointerAttachPos = function(fromX, fromY, anchorPoint)
{
	return this.getHeadPointerAttachPos(fromX, fromY);
}


AnimatedCircle.prototype.getWidth = function()
{
	return this.radius * 2;
}

runtime.AnimatedObject.prototype.setWidth = function(newWidth)
{
	this.radius = newWidth / 2;
}





AnimatedCircle.prototype.getHeadPointerAttachPos = function(fromX, fromY)
{
	var xVec = fromX - this.x;
	var yVec = fromY - this.y;
	var len  = Math.sqrt(xVec * xVec + yVec*yVec);
	if (len == 0)
	{
		return [this.x, this.y];
	}
	return [this.x+(xVec/len)*(this.radius), this.y +(yVec/len)*(this.radius)];
}


AnimatedCircle.prototype.setHighlightIndex = function(hlIndex)
{
    this.highlightIndex = hlIndex;
    this.highlightIndexDirty = true;

}

AnimatedCircle.prototype.draw = function(ctx)
{
	ctx.globalAlpha = this.alpha;

	if (this.highlighted)
	{
		ctx.fillStyle = "#ff0000";
		ctx.beginPath();
		ctx.arc(this.x,this.y,this.radius + this.highlightDiff,0,Math.PI*2, true);
		ctx.closePath();
		ctx.fill();
	}


	ctx.fillStyle = this.backgroundColor;
	ctx.strokeStyle = this.foregroundColor;
	ctx.lineWidth = 1;
	ctx.beginPath();
	ctx.arc(this.x,this.y,this.radius,0,Math.PI*2, true);
	ctx.closePath();
	ctx.fill();
	ctx.stroke();
	ctx.textAlign = 'center';
	ctx.font         = '10px sans-serif';
	ctx.textBaseline   = 'middle';
	ctx.lineWidth = 1;
	ctx.fillStyle = this.foregroundColor;

	var strList = this.label.split("\n");
	if (strList.length == 1)
	{
             if (this.highlightIndexDirty && this.highlightIndex != -1)
             {
                  this.leftWidth = ctx.measureText(this.label.substring(0,this.highlightIndex)).width;
                  this.centerWidth = ctx.measureText(this.label.substring(this.highlightIndex, this.highlightIndex+1)).width;
                  this.textWidth = ctx.measureText(this.label).width;
                  this.highlightIndexDirty = false;
             }
             if (this.highlightIndex != -1 && this.highlightIndex < this.label.length) //this.highlghtIndex < this.label.length)
             {
                     var  startingXForHighlight = this.x - this.textWidth / 2;
    	             ctx.textAlign = 'left';
                    var leftStr = this.label.substring(0, this.highlightIndex);
                    var highlightStr = this.label.substring(this.highlightIndex, this.highlightIndex + 1)
                    var rightStr = this.label.substring(this.highlightIndex + 1)
                    ctx.fillText(leftStr, startingXForHighlight, this.y)
 	            ctx.strokeStyle = "#FF0000";
	            ctx.fillStyle = "#FF0000";
                    ctx.fillText(highlightStr, startingXForHighlight + this.leftWidth, this.y)


	            ctx.strokeStyle = this.labelColor;
	            ctx.fillStyle = this.labelColor;
                    ctx.fillText(rightStr, startingXForHighlight + this.leftWidth + this.centerWidth, this.y)



              }
              else
              {
	    	   ctx.fillText(this.label, this.x, this.y);
              }
	}
	else if (strList.length % 2 == 0)
	{
		var i;
		var mid = strList.length / 2;
		for (i = 0; i < strList.length / 2; i++)
		{
			ctx.fillText(strList[mid - i - 1], this.x, this.y - (i + 0.5) * 12);
			ctx.fillText(strList[mid + i], this.x, this.y + (i + 0.5) * 12);

		}
	}
	else
	{
		var mid = (strList.length - 1) / 2;
		var i;
		ctx.fillText(strList[mid], this.x, this.y);
		for (i = 0; i < mid; i++)
		{
			ctx.fillText(strList[mid - (i + 1)], this.x, this.y - (i + 1) * 12);
			ctx.fillText(strList[mid + (i + 1)], this.x, this.y + (i + 1) * 12);
		}

	}

}


AnimatedCircle.prototype.createUndoDelete = function()
{
	return new UndoDeleteCircle(this.objectID, this.label, this.x, this.y, this.foregroundColor, this.backgroundColor, this.layer, this.radius);
}


function UndoDeleteCircle(id, lab, x, y, foregroundColor, backgroundColor, l, radius)
{
	this.objectID = id;
	this.posX = x;
	this.posY = y;
	this.nodeLabel = lab;
	this.fgColor = foregroundColor;
	this.bgColor = backgroundColor;
	this.layer = l;
        this.radius = radius;
}

UndoDeleteCircle.prototype = new runtime.UndoBlock();
UndoDeleteCircle.prototype.constructor = UndoDeleteCircle;

UndoDeleteCircle.prototype.undoInitialStep = function(world)
{
	world.addCircleObject(this.objectID, this.nodeLabel);
        world.setWidth(this.objectID, this.radius * 2);
	world.setNodePosition(this.objectID, this.posX, this.posY);
	world.setForegroundColor(this.objectID, this.fgColor);
	world.setBackgroundColor(this.objectID, this.bgColor);
	world.setLayer(this.objectID, this.layer);
}





Object.defineProperties(runtime, {
"AnimatedCircle": { configurable: true, get: function() { return AnimatedCircle; }, set: function(value) { AnimatedCircle = value; } },
"UndoDeleteCircle": { configurable: true, get: function() { return UndoDeleteCircle; }, set: function(value) { UndoDeleteCircle = value; } }
});
}


// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco

// Values for xJust / yJust:  "center", "left", "right", "top", "bottom"

function installRectangle(runtime) {
var AnimatedRectangle;
AnimatedRectangle = function(id, val, wth, hgt,  xJust, yJust, fillColor, edgeColor)
{
	this.w = wth;
	this.h = hgt;
	this.xJustify = xJust;
	this.yJustify = yJust;
	this.label = val;
	this.labelColor = edgeColor

	this.backgroundColor = fillColor;
	this.foregroundColor = edgeColor;
	this.labelColor = this.foregroundColor;
	this.highlighted = false;
	this.objectID = id;
	this.nullPointer = false;
	this.alpha = 1.0;
	this.addedToScene = true;

}

AnimatedRectangle.prototype = new runtime.AnimatedObject();
AnimatedRectangle.prototype.constructor = AnimatedRectangle;

AnimatedRectangle.prototype.setNull = function(np)
{
	this.nullPointer = np;
}

AnimatedRectangle.prototype.getNull = function()
{
	return this.nullPointer;
}


AnimatedRectangle.prototype.left = function()
{
	if (this.xJustify == "left")
	{
		return  this.x;
	}
	else if (this.xJustify == "center")
	{
		return this.x - this.w / 2.0;
	}
	else // (this.xJustify == "right")
	{
		return this.x - this.w;
	}

}

AnimatedRectangle.prototype.centerX = function()
{
	if (this.xJustify == "center")
	{
		return this.x;
	}
	else if (this.xJustify == "left")
	{
		return this.x + this.w / 2.0;
	}
	else // (this.xJustify == "right")
	{
		return this.x - this.w / 2.0;
	}
}

AnimatedRectangle.prototype.centerY = function()
{
	if (this.yJustify == "center")
	{
		return this.y;
	}
	else if (this.yJustify == "top")
	{
		return this.y + this.h / 2.0;
	}
	else // (this.xJustify == "bottom")
	{
		return this.y - this.w / 2.0;
	}

}

AnimatedRectangle.prototype.top = function()
{
	if (this.yJustify == "top")
	{
		return  this.y;
	}
	else if (this.yJustify == "center")
	{
		return this.y - this.h / 2.0;
	}
	else //(this.xJustify == "bottom")
	{
		return this.y - this.h;
	}
}

AnimatedRectangle.prototype.bottom = function()
{
	if (this.yJustify == "top")
	{
		return  this.y + this.h;
	}
	else if (this.yJustify == "center")
	{
		return this.y + this.h / 2.0;
	}
	else //(this.xJustify == "bottom")
	{
		return this.y;
	}
}


AnimatedRectangle.prototype.right = function()
{
	if (this.xJustify == "left")
	{
		return  this.x + this.w;
	}
	else if (this.xJustify == "center")
	{
		return this.x + this.w / 2.0;
	}
	else // (this.xJustify == "right")
	{
		return this.x;
	}
}


AnimatedRectangle.prototype.getHeadPointerAttachPos = function(fromX, fromY)
{
	return this.getClosestCardinalPoint(fromX, fromY);
}


AnimatedRectangle.prototype.setWidth = function(wdth)
{
	this.w = wdth;
}


AnimatedRectangle.prototype.setHeight = function(hght)
{
	this.h = hght;
}

AnimatedRectangle.prototype.getWidth = function()
{
	return this.w;
}

AnimatedRectangle.prototype.getHeight = function()
{
	return this.h;
}


// TODO:  Fix me!
AnimatedRectangle.prototype.draw = function(context)
{
	if (!this.addedToScene)
	{
		return;
	}

	var startX;
	var startY;
	var labelPosX;
	var labelPosY;

	context.globalAlpha = this.alpha;

	if (this.xJustify == "left")
	{
		startX = this.x;
		labelPosX = this.x + this.w / 2.0;
	}
	else if (this.xJustify == "center")
	{
		startX = this.x-this.w / 2.0;
		labelPosX = this.x;

	}
	else if (this.xJustify == "right")
	{
		startX = this.x-this.w;
		labelPosX = this.x - this.w / 2.0
	}
	if (this.yJustify == "top")
	{
		startY = this.y;
		labelPosY = this.y + this.h / 2.0;
	}
	else if (this.yJustify == "center")
	{
		startY = this.y - this.h / 2.0;
		labelPosY = this.y;

	}
	else if (this.yJustify == "bottom")
	{
		startY = this.y - this.h;
		labelPosY = this.y - this.h / 2.0;
	}

	context.lineWidth = 1;

	if (this.highlighted)
	{
		context.strokeStyle = "#ff0000";
		context.fillStyle = "#ff0000";

		context.beginPath();
		context.moveTo(startX - this.highlightDiff,startY- this.highlightDiff);
		context.lineTo(startX+this.w + this.highlightDiff,startY- this.highlightDiff);
		context.lineTo(startX+this.w+ this.highlightDiff,startY+this.h + this.highlightDiff);
		context.lineTo(startX - this.highlightDiff,startY+this.h + this.highlightDiff);
		context.lineTo(startX - this.highlightDiff,startY - this.highlightDiff);
		context.closePath();
		context.stroke();
		context.fill();

	}
	context.strokeStyle = this.foregroundColor;
	context.fillStyle = this.backgroundColor;

	context.beginPath();
	context.moveTo(startX ,startY);
	context.lineTo(startX + this.w, startY);
	context.lineTo(startX + this.w, startY + this.h);
	context.lineTo(startX, startY + this.h);
	context.lineTo(startX, startY);
	context.closePath();
	context.stroke();
	context.fill();

	if (this.nullPointer)
	{
		context.beginPath();
		context.moveTo(startX ,startY);
		context.lineTo(startX + this.w, startY + this.h);
		context.closePath();
		context.stroke();
	}

	context.fillStyle = this.labelColor;

	context.textAlign = 'center';
	context.font         = '10px sans-serif';
	context.textBaseline   = 'middle';
	context.lineWidth = 1;
	context.fillText(this.label, this.x, this.y);



}

AnimatedRectangle.prototype.setText = function(newText, textIndex)
{
	this.label = newText;
	// TODO:  setting text position?
}


AnimatedRectangle.prototype.createUndoDelete = function()
{
	// TODO: Add color?
	return new UndoDeleteRectangle(this.objectID, this.label, this.x, this.y, this.w, this.h, this.xJustify, this.yJustify, this.backgroundColor, this.foregroundColor, this.highlighted, this.layer);
}

AnimatedRectangle.prototype.setHighlight = function(value)
{
	this.highlighted = value;
}



function UndoDeleteRectangle(id, lab, x, y, w, h, xJust, yJust, bgColor, fgColor, highlight, lay)
{
	this.objectID = id;
	this.posX = x;
	this.posY = y;
	this.width = w;
	this.height = h;
	this.xJustify = xJust;
	this.yJustify = yJust;
	this.backgroundColor= bgColor;
	this.foregroundColor = fgColor;
	this.nodeLabel = lab;
	this.layer = lay;
	this.highlighted = highlight;
}

UndoDeleteRectangle.prototype = new runtime.UndoBlock();
UndoDeleteRectangle.prototype.constructor = UndoDeleteRectangle;


UndoDeleteRectangle.prototype.undoInitialStep = function(world)
{
	world.addRectangleObject(this.objectID, this.nodeLabel, this.width, this.height, this.xJustify, this.yJustify, this.backgroundColor, this.foregroundColor);
	world.setNodePosition(this.objectID, this.posX, this.posY);
	world.setLayer(this.objectID, this.layer);
	world.setHighlight(this.objectID, this.highlighted);
}




Object.defineProperties(runtime, {
"UndoDeleteRectangle": { configurable: true, get: function() { return UndoDeleteRectangle; }, set: function(value) { UndoDeleteRectangle = value; } },
"AnimatedRectangle": { configurable: true, get: function() { return AnimatedRectangle; }, set: function(value) { AnimatedRectangle = value; } }
});
}


// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco

function installLinkedList(runtime) {
function AnimatedLinkedList(id, val, wth, hgt, linkPer, verticalOrientation, linkPosEnd, numLab, fillColor, edgeColor)
{
	this.init(id, val, wth, hgt, linkPer, verticalOrientation, linkPosEnd, numLab, fillColor, edgeColor);
}

AnimatedLinkedList.prototype = new runtime.AnimatedObject();
AnimatedLinkedList.prototype.constructor = AnimatedLinkedList;
AnimatedLinkedList.superclass = runtime.AnimatedObject.prototype;


AnimatedLinkedList.prototype.init = function(id, val, wth, hgt, linkPer, verticalOrientation, linkPosEnd, numLab, fillColor, edgeColor)
{

	AnimatedLinkedList.superclass.init.call(this);

	this.w = wth;
	this.h = hgt;
	this.backgroundColor = fillColor;
	this.foregroundColor = edgeColor;

	this.vertical = verticalOrientation;
	this.linkPositionEnd = linkPosEnd;
	this.linkPercent = linkPer;

	this.numLabels = numLab;

	this.labels = [];
	this.labelPosX = [];
	this.labelPosY = [];
	this.labelColors = [];
	this.nullPointer = false;

	this.currentHeightDif = 6;
	this.maxHeightDiff = 5;
	this.minHeightDiff = 3;



	for (var i = 0; i < this.numLabels; i++)
	{
		this.labels[i] = "";
		this.labelPosX[i] = 0;
		this.labelPosY[i] = 0;
		this.labelColors[i] = this.foregroundColor;
	}

	this.labels[0] = val;
	this.highlighted = false;
	this.objectID = id;
}



AnimatedLinkedList.prototype.left = function()
{
	if (this.vertical)
	{
		return this.x - this.w / 2.0;
	}
	else if (this.linkPositionEnd)
	{
		return this.x - ((this.w * (1 - this.linkPercent)) / 2);
	}
	else
	{
		return this.x  - (this.w * (this.linkPercent + 1)) / 2;
	}
}


AnimatedLinkedList.prototype.setNull = function(np)
{
	if (this.nullPointer != np)
	{
		this.nullPointer = np;
	}

}

AnimatedLinkedList.prototype.getNull = function()
{
	return this.nullPointer;
}

AnimatedLinkedList.prototype.right = function()
{
	if (this.vertical)
	{
		return this.x + this.w / 2.0;
	}
	else if (this.linkPositionEnd)
	{
		return this.x + ((this.w * (this.linkPercent + 1)) / 2);
	}
	else
	{
		return this.x + (this.w * (1 - this.linkPercent)) / 2;
	}
}

AnimatedLinkedList.prototype.top = function()
{
	if (!this.vertical)
	{
		return this.y - this.h / 2.0;
	}
	else if (this.linkPositionEnd)
	{
		return this.y - (this.h * (1 -this.linkPercent)) / 2;
	}
	else
	{
		return this.y - (this.h * (1 + this.linkPercent)) / 2;
	}
}

AnimatedLinkedList.prototype.bottom = function()
{
	if (!this.vertical)
	{
		return this.y + this.h / 2.0;
	}
	else if (this.linkPositionEnd)
	{
		return this.y + (this.h * (1 +this.linkPercent)) / 2;
	}
	else
	{
		return this.y + (this.h * (1 - this.linkPercent)) / 2;
	}
}


// TODO: Should we move this to the draw function, and save the
//       space of the arrays?  Bit of a leftover from the Flash code,
//       which did drawing differently
AnimatedLinkedList.prototype.resetTextPosition = function()
{
	if (this.vertical)
	{
		this.labelPosX[0] = this.x;

		this.labelPosY[0] = this.y + this.h * (1-this.linkPercent)/2 *(1/this.numLabels - 1);
		//				labelPosY[0] = -height * (1-linkPercent) / 2 + height*(1-linkPercent)/2*numLabels;
		for (var i = 1; i < this.numLabels; i++)
		{
			this.labelPosY[i] = this.labelPosY[i-1] +  this.h*(1-this.linkPercent)/this.numLabels;
			this.labelPosX[i] = this.x;
		}
	}
	else
	{
		this.labelPosY[0] = this.y;
		this.labelPosX[0] = this.x +  this.w * (1-this.linkPercent)/2*(1/this.numLabels - 1);
		for (var i = 1; i < this.numLabels; i++)
		{
			this.labelPosY[i] = this.y;
			this.labelPosX[i] = this.labelPosX[i-1] +  this.w*(1-this.linkPercent)/this.numLabels;
		}
	}

}


AnimatedLinkedList.prototype.getTailPointerAttachPos = function(fromX, fromY, anchor)
{
	if (this.vertical && this.linkPositionEnd)
	{
		return [this.x, this.y + this.h / 2.0];
	}
	else if (this.vertical && !this.linkPositionEnd)
	{
		return [this.x, this.y - this.h / 2.0];
	}
	else if  (!this.vertical && this.linkPositionEnd)
	{
		return [this.x + this.w / 2.0, this.y];
	}
	else // (!this.vertical && !this.linkPositionEnd)
	{
		return [this.x - this.w / 2.0, this.y];
	}
}


AnimatedLinkedList.prototype.getHeadPointerAttachPos = function(fromX, fromY)
{
	return this.getClosestCardinalPoint(fromX, fromY);
}


AnimatedLinkedList.prototype.setWidth = function(wdth)
{
	this.w = wdth;
	this.resetTextPosition();
}


AnimatedLinkedList.prototype.setHeight = function(hght)
{
	this.h = hght;
	this.resetTextPosition();
}

AnimatedLinkedList.prototype.getWidth = function()
{
	return this.w;
}

AnimatedLinkedList.prototype.getHeight = function()
{
	return this.h;
}

AnimatedLinkedList.prototype.draw = function(context)
{
	var startX;
	var startY;

	startX = this.left();
	startY = this.top();

	if (this.highlighted)
	{
		context.strokeStyle = "#ff0000";
		context.fillStyle = "#ff0000";

		context.beginPath();
		context.moveTo(startX - this.highlightDiff,startY- this.highlightDiff);
		context.lineTo(startX+this.w + this.highlightDiff,startY- this.highlightDiff);
		context.lineTo(startX+this.w+ this.highlightDiff,startY+this.h + this.highlightDiff);
		context.lineTo(startX - this.highlightDiff,startY+this.h + this.highlightDiff);
		context.lineTo(startX - this.highlightDiff,startY - this.highlightDiff);
		context.closePath();
		context.stroke();
		context.fill();
	}
	context.strokeStyle = this.foregroundColor;
	context.fillStyle = this.backgroundColor;

	context.beginPath();
	context.moveTo(startX ,startY);
	context.lineTo(startX + this.w, startY);
	context.lineTo(startX + this.w, startY + this.h);
	context.lineTo(startX, startY + this.h);
	context.lineTo(startX, startY);
	context.closePath();
	context.stroke();
	context.fill();

	var i;
	if (this.vertical)
	{
		startX = this.left();
		for (i= 1; i < this.numLabels; i++)
		{
			//TODO: this doesn't look right ...
			startY = this.y + this.h*(1-this.linkPercent)*(i / this.numLabels - 1/2);

			context.beginPath();
			context.moveTo(startX ,startY);
			context.lineTo(startX + this.w, startY);
			context.closePath();
			context.stroke();
		}
	}
	else
	{
		startY = this.top();
		for (i = 1; i < this.numLabels; i++)
		{
			startX = this.x + this.w*(1-this.linkPercent)*(i / this.numLabels - 1/2);
			context.beginPath();
			context.moveTo(startX ,startY);
			context.lineTo(startX, startY + this.h);
			context.closePath();
			context.stroke();
		}
	}

	if (this.vertical && this.linkPositionEnd)
	{
		startX = this.left();
		startY = this.bottom() - this.h * this.linkPercent;


		context.beginPath();
		context.moveTo(startX + this.w ,startY);
		context.lineTo(startX, startY);
		if (this.nullPointer)
		{
			context.lineTo(this.startX + this.w, this.bottom());
		}
		context.closePath();
		context.stroke();
	}
	else if (this.vertical && !this.linkPositionEnd)
	{
		startX = this.left();
		startY = this.top() + this.h * this.linkPercent;

		context.beginPath();
		context.moveTo(startX + this.w ,startY);
		context.lineTo(startX, startY);
		if (this.nullPointer)
		{
			context.lineTo(startX + this.w, this.top());
		}
		context.closePath();
		context.stroke();

	}
	else if  (!this.vertical && this.linkPositionEnd)
	{
		startX = this.right() - this.w * this.linkPercent;
		startY = this.top();

		context.beginPath();
		context.moveTo(startX, startY + this.h);
		context.lineTo(startX, startY);
		if (this.nullPointer)
		{
			context.lineTo(this.right(), startY + this.h);
		}
		context.closePath();
		context.stroke();

	}
	else // (!vertical && !linkPositionEnd)
	{
		startX = this.left()  + this.w * this.linkPercent;
		startY = this.top() ;

		context.beginPath();
		context.moveTo(startX, startY + this.h);
		context.lineTo(startX, startY);
		if (this.nullPointer)
		{
			context.lineTo(this.left(), startY);
		}
		context.closePath();
		context.stroke();
	}


	context.textAlign = 'center';
	context.font         = '10px sans-serif';
	context.textBaseline   = 'middle';
	context.lineWidth = 1;


	this.resetTextPosition();
	for (i = 0; i < this.numLabels; i++)
	{
		context.fillStyle = this.labelColors[i];
		context.fillText(this.labels[i], this.labelPosX[i], this.labelPosY[i]);
	}
}



AnimatedLinkedList.prototype.setTextColor = function(color, textIndex)
{

	this.labelColors[textIndex] = color;
}



AnimatedLinkedList.prototype.getTextColor = function(textIndex)
{
	return this.labelColors[textIndex];
}



AnimatedLinkedList.prototype.getText = function(index)
{
	return this.labels[index];
}

AnimatedLinkedList.prototype.setText = function(newText, textIndex)
{
	this.labels[textIndex] = newText;
	this.resetTextPosition();
}







AnimatedLinkedList.prototype.createUndoDelete = function()
{
	return new UndoDeleteLinkedList(this.objectID, this.numLabels, this.labels, this.x, this.y, this.w, this.h, this.linkPercent,
									this.linkPositionEnd, this.vertical, this.labelColors, this.backgroundColor, this.foregroundColor,
									this.layer, this.nullPointer);
}

AnimatedLinkedList.prototype.setHighlight = function(value)
{
	if (value != this.highlighted)
	{
		this.highlighted = value;
	}
}




function UndoDeleteLinkedList(id, numlab, lab, x, y, w, h, linkper, posEnd, vert, labColors, bgColor, fgColor, l, np)
{
	this.objectID = id;
	this.posX = x;
	this.posY = y;
	this.width = w;
	this.height = h;
	this.backgroundColor= bgColor;
	this.foregroundColor = fgColor;
	this.labels = lab;
	this.linkPercent = linkper;
	this.verticalOrentation = vert;
	this.linkAtEnd = posEnd;
	this.labelColors = labColors
	this.layer = l;
	this.numLabels = numlab;
	this.nullPointer = np;
}

UndoDeleteLinkedList.prototype = new runtime.UndoBlock();
UndoDeleteLinkedList.prototype.constructor = UndoDeleteLinkedList;



UndoDeleteLinkedList.prototype.undoInitialStep =function(world)
{
	world.addLinkedListObject(this.objectID,this.labels[0], this.width, this.height, this.linkPercent, this.verticalOrentation, this.linkAtEnd, this.numLabels, this.backgroundColor, this.foregroundColor);
	world.setNodePosition(this.objectID, this.posX, this.posY);
	world.setLayer(this.objectID, this.layer);
	world.setNull(this.objectID, this.nullPointer);
	for (var i = 0; i < this.numLabels; i++)
	{
		world.setText(this.objectID, this.labels[i], i);
		world.setTextColor(this.objectID, this.labelColors[i], i);
	}
}

Object.defineProperties(runtime, {
"AnimatedLinkedList": { configurable: true, get: function() { return AnimatedLinkedList; }, set: function(value) { AnimatedLinkedList = value; } },
"UndoDeleteLinkedList": { configurable: true, get: function() { return UndoDeleteLinkedList; }, set: function(value) { UndoDeleteLinkedList = value; } }
});
}


﻿// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco


function installBTreeNode(runtime) {
function AnimatedBTreeNode(id, widthPerElem, h, numElems,  fillColor, edgeColor)
{
	fillColor = (fillColor == undefined)? "#FFFFFF" : fillColor;
	edgeColor = (edgeColor == undefined)? "#000000" : edgeColor;
	this.init(id, widthPerElem, h, numElems,  fillColor, edgeColor);
}

AnimatedBTreeNode.prototype = new runtime.AnimatedObject();
AnimatedBTreeNode.prototype.constructor = AnimatedBTreeNode;
AnimatedBTreeNode.superclass = runtime.AnimatedObject.prototype;

AnimatedBTreeNode.MIN_WIDTH = 10;
AnimatedBTreeNode.EDGE_POINTER_DISPLACEMENT = 5;


AnimatedBTreeNode.prototype.init = function(id, widthPerElem, h, numElems,  fillColor, edgeColor)
{

	var  MIN_WIDTH = 10;
	AnimatedBTreeNode.superclass.init.call(this);
	this.objectID = id;


	this.backgroundColor = fillColor;
	this.foregroundColor = edgeColor;

	this.widthPerElement = widthPerElem;
	this.nodeHeight = h;
	this.numLabels = numElems;
	this.labels = new Array(this.numLabels);
	this.labelColors = new Array(this.numLabels);
	for (var i = 0; i < this.numLabels; i++)
	{
		this.labelColors[i] = this.foregroundColor;
	}
}

AnimatedBTreeNode.prototype.getNumElements = function()
{
	return this.numLabels;
}

AnimatedBTreeNode.prototype.getWidth = function()
{
	if (this.numLabels > 0)
	{
		return  (this.widthPerElement * this.numLabels);
	}
	else
	{
		return AnimatedBTreeNode.MIN_WIDTH;
	}
}


AnimatedBTreeNode.prototype.setNumElements = function(newNumElements)
{
	var i;
	if (this.numLabels < newNumElements)
	{
		for (i = this.numLabels; i < newNumElements; i++)
		{
			this.labels[i] = "";
			this.labelColors[i] = this.foregroundColor;
		}
		this.numLabels = newNumElements;
	}
	else if (this.numLabels > newNumElements)
	{
		for (i = newNumElements; i < this.numLabels; i++)
		{
			this.labels[i] = null;
		}
		this.numLabels = newNumElements;
	}
}


AnimatedBTreeNode.prototype.left = function()
{
	return this.x  - this.getWidth() / 2.0;
}

AnimatedBTreeNode.prototype.right = function()
{
	return this.x  + this.getWidth() / 2.0;
}

AnimatedBTreeNode.prototype.top = function()
{
	return this.y - this.nodeHeight / 2.0;
}

AnimatedBTreeNode.prototype.bottom = function()
{
	return this.y + this.nodeHeight / 2.0;
}


AnimatedBTreeNode.prototype.draw = function(context)
{
	var startX;
	var startY;

	startX = this.left();
	if (startX == NaN)
	{
		startX  = 0;
	}
	startY = this.top();

	if (this.highlighted)
	{
		context.strokeStyle = "#ff0000";
		context.fillStyle = "#ff0000";

		context.beginPath();
		context.moveTo(startX - this.highlightDiff,startY- this.highlightDiff);
		context.lineTo(startX+this.getWidth() + this.highlightDiff,startY- this.highlightDiff);
		context.lineTo(startX+this.getWidth() + this.highlightDiff,startY+this.nodeHeight + this.highlightDiff);
		context.lineTo(startX - this.highlightDiff,startY+this.nodeHeight + this.highlightDiff);
		context.lineTo(startX - this.highlightDiff,startY - this.highlightDiff);
		context.closePath();
		context.stroke();
		context.fill();
	}

	context.strokeStyle = this.foregroundColor;
	context.fillStyle = this.backgroundColor;

	context.beginPath();
	context.moveTo(startX ,startY);
	context.lineTo(startX + this.getWidth(), startY);
	context.lineTo(startX + this.getWidth(), startY + this.nodeHeight);
	context.lineTo(startX, startY + this.nodeHeight);
	context.lineTo(startX, startY);
	context.closePath();
	context.stroke();
	context.fill();

	context.textAlign = 'center';
	context.textBaseline   = 'middle';


	for (var i = 0; i < this.numLabels; i++)
	{
		var labelx  = this.x - this.widthPerElement * this.numLabels / 2 + this.widthPerElement / 2 + i * this.widthPerElement;
		var labely = this.y

		context.fillStyle = this.labelColors[i];
		context.fillText(this.labels[i], labelx, labely);
	}
}



AnimatedBTreeNode.prototype.getHeight = function()
{
	return this.nodeHeight;
}



AnimatedBTreeNode.prototype.setForegroundColor = function(newColor)
{
	this.foregroundColor = newColor;
	for (var i = 0; i < this.numLabels; i++)
	{
		this.labelColors[i] = newColor;
	}
}


// TODO:  Kill the magic numbers here
AnimatedBTreeNode.prototype.getTailPointerAttachPos = function(fromX, fromY, anchor)
{
	if (anchor == 0)
	{
		return [this.left() + AnimatedBTreeNode.EDGE_POINTER_DISPLACEMENT, this.y];
	}
	else if (anchor == this.numLabels)
	{
		return [this.right() - AnimatedBTreeNode.EDGE_POINTER_DISPLACEMENT, this.y];
	}
	else
	{
		return [this.left() + anchor * this.widthPerElement, this.y]
	}
}


AnimatedBTreeNode.prototype.getHeadPointerAttachPos = function(fromX, fromY)
{
	if (fromY < this.y - this.nodeHeight / 2)
	{
		return [this.x, this.y - this.nodeHeight / 2];
	}
	else if (this.fromY > this.y + this.nodeHeight /  2)
	{
		return [this.x, this.y + this.nodeHeight / 2];
	}
	else if (fromX  <  this.x  - this.getWidth() / 2)
	{
		return [this.x - this.getWidth() / 2, this.y];
	}
	else
	{
		return [this.x + this.getWidth() / 2, this.y];
	}
}



AnimatedBTreeNode.prototype.createUndoDelete = function()
{
	return new UndoDeleteBTreeNode(this.objectID, this.numLabels, this.labels, this.x, this.y, this.widthPerElement, this.nodeHeight, this.labelColors, this.backgroundColor, this.foregroundColor, this.layer, this.highlighted);
}


AnimatedBTreeNode.prototype.getTextColor = function(textIndex)
{
	textIndex = (textIndex == undefined) ? 0 : textIndex;
	return this.labelColors[textIndex];
}

AnimatedBTreeNode.prototype.getText = function(index)
{
	index = (index == undefined) ? 0 : index;
	return this.labels[index];
}

AnimatedBTreeNode.prototype.setTextColor = function(color, textIndex)
{
	textIndex = (textIndex == undefined) ? 0 : textIndex;
	this.labelColors[textIndex] = color;
}


AnimatedBTreeNode.prototype.setText = function(newText, textIndex)
{
	textIndex = (textIndex == undefined) ? 0 : textIndex;
	this.labels[textIndex] = newText;
}



function UndoDeleteBTreeNode(id, numLab, labelText, x, y, wPerElement, nHeight, lColors, bgColor, fgColor, l, highlighted)
{
	this.objectID = id;
	this.posX = x;
	this.posY = y;
	this.widthPerElem = wPerElement;
	this.nodeHeight = nHeight;
	this.backgroundColor= bgColor;
	this.foregroundColor = fgColor;
	this.numElems = numLab;
	this.labels = labelText;

	this.labelColors = lColors;
	this.layer = l;
	this.highlighted = highlighted;
}

UndoDeleteBTreeNode.prototype = new runtime.UndoBlock();
UndoDeleteBTreeNode.prototype.constructor = UndoDeleteBTreeNode;

UndoDeleteBTreeNode.prototype.undoInitialStep = function(world)
{

	world.addBTreeNode(this.objectID, this.widthPerElem, this.nodeHeight, this.numElems, this.backgroundColor, this.foregroundColor);
	world.setNodePosition(this.objectID, this.posX, this.posY);
	for (var i = 0; i < this.numElems; i++)
	{
		world.setText(this.objectID, this.labels[i], i);
		world.setTextColor(this.objectID, this.labelColors[i],i);
	}
	world.setHighlight(this.objectID, this.highlighted);
	world.setLayer(this.objectID, this.layer);
}




Object.defineProperties(runtime, {
"AnimatedBTreeNode": { configurable: true, get: function() { return AnimatedBTreeNode; }, set: function(value) { AnimatedBTreeNode = value; } },
"UndoDeleteBTreeNode": { configurable: true, get: function() { return UndoDeleteBTreeNode; }, set: function(value) { UndoDeleteBTreeNode = value; } }
});
}


// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco

// "Class" animatedCircle


function installHighlight(runtime) {
var HighlightCircle = function(objectID, color, radius)
{
	this.objectID = objectID;
	this.radius = radius;
	this.thickness = 4;
	this.foregroundColor = color;
	this.x = 0;
	this.y = 0;
	this.alpha = 1;
}

HighlightCircle.prototype = new runtime.AnimatedObject();
HighlightCircle.prototype.constructor = HighlightCircle;


HighlightCircle.prototype.draw = function(ctx)
{
	ctx.globalAlpha = this.alpha;
	ctx.strokeStyle = this.foregroundColor;
	ctx.lineWidth = this.thickness;
	ctx.beginPath();
	ctx.arc(this.x,this.y,this.radius,0,Math.PI*2, true);
	ctx.closePath();
	ctx.stroke();
}


HighlightCircle.prototype.createUndoDelete = function()
{
	return new UndoDeleteHighlightCircle(this.objectID, this.x, this.y, this.foregroundColor, this.radius, this.layer, this.alpha);
}


function UndoDeleteHighlightCircle(objectID, x, y, circleColor, r, layer, alpha)
{
	this.objectID = objectID;
	this.x = x;
	this.y = y;
	this.color = circleColor;
	this.r = r;
	this.layer = layer;
	this.alpha = alpha
}

UndoDeleteHighlightCircle.prototype = new runtime.UndoBlock();
UndoDeleteHighlightCircle.prototype.constructor = UndoDeleteHighlightCircle;

UndoDeleteHighlightCircle.prototype.undoInitialStep = function(world)
{
	world.addHighlightCircleObject(this.objectID, this.color, this.r);
	world.setLayer(this.objectID, this.layer)
	world.setNodePosition(this.objectID, this.x, this.y);
	world.setAlpha(this.objectID, this.alpha)
}





Object.defineProperties(runtime, {
"HighlightCircle": { configurable: true, get: function() { return HighlightCircle; }, set: function(value) { HighlightCircle = value; } },
"UndoDeleteHighlightCircle": { configurable: true, get: function() { return UndoDeleteHighlightCircle; }, set: function(value) { UndoDeleteHighlightCircle = value; } }
});
}


// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco


// This class is somewhat poorly named -- it handles links between vertices in graphs,
//  pointers in linked lists, and so on.


function installLine(runtime) {
var Dirty;
var LINE_maxHeightDiff = 5;
var LINE_minHeightDiff = 3;
var LINE_range= LINE_maxHeightDiff - LINE_minHeightDiff + 1;
var LINE_highlightDiff = 3;


function Line(n1, n2, color, cv, d, weight, anchorIndex)
{
	this.arrowHeight = 8;
	this. arrowWidth = 4;

	this.Node1 = n1;
	this.Node2 = n2;
	this.Dirty = false;
	this.directed = d;
	this.edgeColor = color;
	this.edgeLabel = weight;
	this.highlighted = false;
	this.addedToScene = true;
	this.anchorPoint = anchorIndex;
	this.highlightDiff = 0;
	this.curve = cv;

	this.alpha = 1.0;
	this.color = function color()
	{
		return this.edgeColor;
	}

	this.setColor = function(newColor)
	{
		this.edgeColor = newColor;
		Dirty = true;
	}

	this.setHighlight = function(highlightVal)
	{
		this.highlighted = highlightVal;
	}

	this.pulseHighlight = function(frameNum)
	{
	   if (this.highlighted)
	   {
		   var frameMod = frameNum / 14.0;
		   var delta  = Math.abs((frameMod) % (2 * LINE_range  - 2) - LINE_range + 1)
		   this.highlightDiff =  delta + LINE_minHeightDiff;
		   Dirty = true;
	   }
	}


	this.hasNode = function(n)
	{
		return ((this.Node1 == n) || (this.Node2 == n));
	}


	this.createUndoDisconnect  = function()
        {
		return new UndoConnect(this.Node1.objectID, this.Node2.objectID, true, this.edgeColor, this.directed, this.curve, this.edgeLabel, this.anchorPoint);
	}


	this.sign = function(n)
	{
	   if (n > 0)
	   {
		   return 1;
	   }
	   else
	   {
		   return -1;
	   }
	}


	this.drawArrow = function(pensize, color, context)
	{
		context.strokeStyle = color;
		context.fillStyle = color;
		context.lineWidth = pensize;
		var fromPos = this.Node1.getTailPointerAttachPos(this.Node2.x, this.Node2.y, this.anchorPoint);
		var toPos = this.Node2.getHeadPointerAttachPos(this.Node1.x, this.Node1.y);

		var fromPos = this.Node1.getTailPointerAttachPos(this.Node2.x, this.Node2.y, this.anchorPoint);
		var toPos = this.Node2.getHeadPointerAttachPos(this.Node1.x, this.Node1.y);

		var deltaX = toPos[0] - fromPos[0];
		var deltaY = toPos[1] - fromPos[1];
		var midX = (deltaX) / 2.0 + fromPos[0];
		var midY = (deltaY) / 2.0 + fromPos[1];
		var controlX = midX - deltaY * this.curve;

		var controlY = midY + deltaX * this.curve;

		context.beginPath();
		context.moveTo(fromPos[0], fromPos[1]);
		context.quadraticCurveTo(controlX, controlY, toPos[0], toPos[1]);
		context.stroke();
		//context.closePath();

		// Position of the edge label:  First, we will place it right along the
		// middle of the curve (or the middle of the line, for curve == 0)
		var labelPosX = 0.25* fromPos[0] + 0.5*controlX + 0.25*toPos[0];
		var labelPosY =  0.25* fromPos[1] + 0.5*controlY + 0.25*toPos[1];

		// Next, we push the edge position label out just a little in the direction of
		// the curve, so that the label doesn't intersect the cuve (as long as the label
		// is only a few characters, that is)
		var midLen = Math.sqrt(deltaY*deltaY + deltaX*deltaX);
		if (midLen != 0)
		{
			labelPosX +=  (- deltaY * this.sign(this.curve))  / midLen * 10
			labelPosY += ( deltaX * this.sign(this.curve))  / midLen * 10
		}



		context.textAlign = 'center';
		context.font         = '10px sans-serif';
		context.textBaseline   = 'middle';
		context.fillText(this.edgeLabel, labelPosX, labelPosY);

		if (this.directed)
		{
			var xVec = controlX - toPos[0];
			var yVec = controlY - toPos[1];
			var len = Math.sqrt(xVec * xVec + yVec*yVec);

			if (len > 0)
			{
				xVec = xVec / len
				yVec = yVec / len;

				context.beginPath();
				context.moveTo(toPos[0], toPos[1]);
				context.lineTo(toPos[0] + xVec*this.arrowHeight - yVec*this.arrowWidth, toPos[1] + yVec*this.arrowHeight + xVec*this.arrowWidth);
				context.lineTo(toPos[0] + xVec*this.arrowHeight + yVec*this.arrowWidth, toPos[1] + yVec*this.arrowHeight - xVec*this.arrowWidth);
				context.lineTo(toPos[0], toPos[1]);
				context.closePath();
				context.stroke();
				context.fill();
			}

		}

	   }


	   this.draw = function(ctx)
	   {
		   if (!this.addedToScene)
		   {
			   return;
		   }
		   ctx.globalAlpha = this.alpha;

			if (this.highlighted)
				this.drawArrow(this.highlightDiff, "#FF0000", ctx);
			this.drawArrow(1, this.edgeColor, ctx);
	   }


}



function UndoConnect(from, to, createConnection, edgeColor, isDirected, cv, lab, anch)
{
	this.fromID = from;
	this.toID = to;
	this.connect = createConnection;
	this.color = edgeColor;
	this.directed = isDirected;
	this.curve = cv;
	this.edgeLabel = lab;
	this.anchorPoint = anch;
}


UndoConnect.prototype.undoInitialStep = function(world)
{
	if (this.connect)
	{
		world.connectEdge(this.fromID, this.toID, this.color, this.curve, this.directed, this.edgeLabel,this.anchorPoint);
	}
	else
	{
		world.disconnect(this.fromID,this.toID);
	}
}


UndoConnect.prototype.addUndoAnimation = function(animationList)
{
	return false;
}

Object.defineProperties(runtime, {
"LINE_maxHeightDiff": { configurable: true, get: function() { return LINE_maxHeightDiff; }, set: function(value) { LINE_maxHeightDiff = value; } },
"LINE_minHeightDiff": { configurable: true, get: function() { return LINE_minHeightDiff; }, set: function(value) { LINE_minHeightDiff = value; } },
"LINE_range": { configurable: true, get: function() { return LINE_range; }, set: function(value) { LINE_range = value; } },
"LINE_highlightDiff": { configurable: true, get: function() { return LINE_highlightDiff; }, set: function(value) { LINE_highlightDiff = value; } },
"Line": { configurable: true, get: function() { return Line; }, set: function(value) { Line = value; } },
"UndoConnect": { configurable: true, get: function() { return UndoConnect; }, set: function(value) { UndoConnect = value; } }
});
}


// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco


// Object Manager
//
// Manage all of our animated objects.  Control any animated object should occur through
// this interface (not language enforced, because enforcing such things in Javascript is
// problematic.)
//
// This class is only accessed through:
//
//  AnimationMain
//  Undo objects (which are themselves controlled by AnimationMain


// TODO:
//       1.  Add proper throws for all error conditions (perhaps guarded by
//           an assert-like structure that can be turned off for production?)
//       2.  Refactor this code so that it uses the same object syntax (with
//           prototypes) as te rest of the code.  (low priority)
function installObjects(runtime) {
var w;
function ObjectManager()
{
	this.Nodes = [];
	this.Edges = [];
	this.BackEdges = [];
	this.activeLayers = [];
	this.activeLayers[0] = true;
	this.ctx = runtime.document.getElementById('canvas').getContext('2d');
	this.framenum = 0;
	this.width = 0;
	this.height = 0;
	this.statusReport = new runtime.AnimatedLabel(-1, "XXX", false, 30);
	this.statusReport.x = 30;

	this.draw = function()
	{
		this.framenum++;
		if (this.framenum > 1000)
			this.framenum = 0;

		this.ctx.clearRect(0,0,this.width,this.height); // clear canvas
		this.statusReport.y = this.height - 15;

		var i;
		var j;
		for (i = 0; i < this.Nodes.length; i++)
		{
			if (this.Nodes[i] != null && !this.Nodes[i].highlighted && this.Nodes[i].addedToScene && !this.Nodes[i].alwaysOnTop)
			{
				this.Nodes[i].draw(this.ctx);
			}
		}
		for (i = 0; i < this.Nodes.length; i++)
		{
			if (this.Nodes[i] != null && (this.Nodes[i].highlighted && !this.Nodes[i].alwaysOnTop) && this.Nodes[i].addedToScene)
			{
				this.Nodes[i].pulseHighlight(this.framenum);
				this.Nodes[i].draw(this.ctx);
			}
		}

		for (i = 0; i < this.Nodes.length; i++)
		{
			if (this.Nodes[i] != null && this.Nodes[i].alwaysOnTop && this.Nodes[i].addedToScene)
			{
				this.Nodes[i].pulseHighlight(this.framenum);
				this.Nodes[i].draw(this.ctx);
			}
		}


		for (i = 0; i < this.Edges.length; i++)
		{
			if (this.Edges[i] != null)
			{
				for (j = 0; j < this.Edges[i].length; j++)
				{
					if (this.Edges[i][j].addedToScene)
					{
						this.Edges[i][j].pulseHighlight(this.framenum);
						this.Edges[i][j].draw(this.ctx);
					}

				}

			}
		}
		this.statusReport.draw(this.ctx);

	}

	this.update = function ()
	{


	}


	this.setLayers = function(shown,layers)
	{
		for (var i = 0; i < layers.length; i++)
		{
			this.activeLayers[layers[i]] = shown;
		}
		this.resetLayers();

	}


	this.addHighlightCircleObject = function(objectID, objectColor, radius)
	{
		if (this.Nodes[objectID] != null && this.Nodes[objectID] != undefined)
		{
  	            throw "addHighlightCircleObject:Object with same ID (" + String(objectID) + ") already Exists!"
		}
		var newNode = new runtime.HighlightCircle(objectID, objectColor, radius)
		this.Nodes[objectID] = newNode;
	}

	this.setEdgeAlpha = function(fromID, toID, alphaVal)
	{
		var oldAlpha = 1.0;
		if (this.Edges[fromID] != null &&
			this.Edges[fromID] != undefined)
		{
			var len = this.Edges[fromID].length;
			for (var i = len - 1; i >= 0; i--)
			{
				if (this.Edges[fromID][i] != null &&
					this.Edges[fromID][i] != undefined &&
					this.Edges[fromID][i].Node2 == this.Nodes[toID])
				{
					oldAlpha = this.Edges[fromID][i].alpha
					this.Edges[fromID][i].alpha = alphaVal;
				}
			}
		}
		return oldAlpha;

	}

	this.setAlpha = function(nodeID, alphaVal)
	{
		if (this.Nodes[nodeID] != null && this.Nodes[nodeID] != undefined)
		{
			this.Nodes[nodeID].setAlpha(alphaVal);
		}
	}

	this.getAlpha = function(nodeID)
	{
		if (this.Nodes[nodeID] != null && this.Nodes[nodeID] != undefined)
		{
			return this.Nodes[nodeID].getAlpha();
		}
		else
		{
			return -1;
		}
	}

	this.getTextColor = function(nodeID, index)
	{
		if (this.Nodes[nodeID] != null && this.Nodes[nodeID] != undefined)
		{
			return this.Nodes[nodeID].getTextColor(index);
		}
		else
		{
			return "#000000";
		}

	}

	this.setTextColor = function(nodeID, color, index)
	{
		if (this.Nodes[nodeID] != null && this.Nodes[nodeID] != undefined)
		{
			this.Nodes[nodeID].setTextColor(color, index);
		}
	}


	this.setHighlightIndex = function(nodeID, index)
	{
		if (this.Nodes[nodeID] != null && this.Nodes[nodeID] != undefined)
		{
			this.Nodes[nodeID].setHighlightIndex(index);
		}
	}





	this.setAllLayers = function(layers)
	{
		this.activeLayers = [];
		for(var i = 0; i < layers.length; i++)
		{
			this.activeLayers[layers[i]] = true;
		}
		this.resetLayers();
	}

	this.resetLayers = function()
	{
		var i
		for (i = 0; i <this.Nodes.length; i++)
		{
			if (this.Nodes[i] != null && this.Nodes[i] != undefined)
			{
				this.Nodes[i].addedToScene = this.activeLayers[this.Nodes[i].layer] == true;
			}
		}
		for (i = this.Edges.length - 1; i >= 0; i--)
		{
		    if (this.Edges[i] != null && this.Edges[i] != undefined)
			{
				for (var j = 0; j < this.Edges[i].length; j++)
				{
					if (this.Edges[i][j] != null && this.Edges[i][j] != undefined)
					{
							this.Edges[i][j].addedToScene =
								this.activeLayers[this.Edges[i][j].Node1.layer] == true &&
								this.activeLayers[this.Edges[i][j].Node2.layer] == true;
					}

				}

			}

		}

	}



	this.setLayer = function(objectID, layer)
	{
		if (this.Nodes[objectID] != null && this.Nodes[objectID] != undefined)
		{
			this.Nodes[objectID].layer = layer;
			if (this.activeLayers[layer])
			{
				this.Nodes[objectID].addedToScene = true;
			}
			else
			{
				this.Nodes[objectID].addedToScene = false;
			}
			if (this.Edges[objectID] != null && this.Edges[objectID] != undefined)
			{
				for (var i = 0; i < this.Edges[objectID].length; i++)
				{
					var nextEdge = this.Edges[objectID][i];
					if (nextEdge != null && nextEdge != undefined)
					{
						nextEdge.addedToScene = ((nextEdge.Node1.addedToScene) &&
												(nextEdge.Node2.addedToScene));

					}
				}
			}
			if (this.BackEdges[objectID] != null && this.BackEdges[objectID] != undefined)
			{
				for (var i = 0; i < this.BackEdges[objectID].length; i++)
				{
					var nextEdge = this.BackEdges[objectID][i];
					if (nextEdge != null && nextEdge != undefined)
					{
						nextEdge.addedToScene = ((nextEdge.Node1.addedToScene) &&
												 (nextEdge.Node2.addedToScene));

					}
				}
			}
		}
	}

	this.clearAllObjects = function()
	{
		this.Nodes = [];
		this.Edges = [];
		this.BackEdges = [];
	}


	this.setForegroundColor = function(objectID, color)
	{
		if (this.Nodes[objectID] != null && this.Nodes[objectID] != undefined)
		{
			this.Nodes[objectID].setForegroundColor(color);

		}
	}

	this.setBackgroundColor = function(objectID, color)
	{
		if (this.Nodes[objectID] != null)
		{
			this.Nodes[objectID].setBackgroundColor(color);

		}
	}

	this.setHighlight = function(nodeID, val)
	{
		if (this.Nodes[nodeID] == null  || this.Nodes[nodeID] == undefined)
		{
			// TODO:  Error here?
			return;
		}
		this.Nodes[nodeID].setHighlight(val);
	}


	this.getHighlight = function(nodeID)
	{
		if (this.Nodes[nodeID] == null  || this.Nodes[nodeID] == undefined)
		{
			// TODO:  Error here?
			return false;
		}
		return this.Nodes[nodeID].getHighlight();
	}


	this.getHighlightIndex = function(nodeID)
	{
		if (this.Nodes[nodeID] == null  || this.Nodes[nodeID] == undefined)
		{
			// TODO:  Error here?
			return false;
		}
		return this.Nodes[nodeID].getHighlightIndex();
	}

	this.setWidth = function(nodeID, val)
	{
		if (this.Nodes[nodeID] == null  || this.Nodes[nodeID] == undefined)
		{
			// TODO:  Error here?
			return;
		}
		this.Nodes[nodeID].setWidth(val);
	}

	this.setHeight = function(nodeID, val)
	{
		if (this.Nodes[nodeID] == null  || this.Nodes[nodeID] == undefined)
		{
			// TODO:  Error here?
			return;
		}
		this.Nodes[nodeID].setHeight(val);
	}


	this.getHeight = function(nodeID)
	{
		if (this.Nodes[nodeID] == null  || this.Nodes[nodeID] == undefined)
		{
			// TODO:  Error here?
			return -1;
		}
		return this.Nodes[nodeID].getHeight();
	}

	this.getWidth = function(nodeID)
	{
		if (this.Nodes[nodeID] == null  || this.Nodes[nodeID] == undefined)
		{
			// TODO:  Error here?
			return -1;
		}
		return this.Nodes[nodeID].getWidth();
	}

	this.backgroundColor = function(objectID)
	{
		if (this.Nodes[objectID] != null)
		{
			return this.Nodes[objectID].backgroundColor;
		}
		else
		{
			return '#000000';
		}
	}

	this.foregroundColor = function(objectID)
	{
		if (this.Nodes[objectID] != null)
		{
			return this.Nodes[objectID].foregroundColor;
		}
		else
		{
			return '#000000';
		}
	}


	this.disconnect = function(objectIDfrom,objectIDto)
	{
		var undo = null;
		var i;
		if (this.Edges[objectIDfrom] != null)
		{
			var len = this.Edges[objectIDfrom].length;
			for (i = len - 1; i >= 0; i--)
			{
				if (this.Edges[objectIDfrom][i] != null && this.Edges[objectIDfrom][i].Node2 == this.Nodes[objectIDto])
				{
					var deleted = this.Edges[objectIDfrom][i];
					undo = deleted.createUndoDisconnect();
					this.Edges[objectIDfrom][i] = this.Edges[objectIDfrom][len - 1];
					len -= 1;
					this.Edges[objectIDfrom].pop();
				}
			}
		}
		if (this.BackEdges[objectIDto] != null)
		{
			len = this.BackEdges[objectIDto].length;
			for (i = len - 1; i >= 0; i--)
			{
				if (this.BackEdges[objectIDto][i] != null && this.BackEdges[objectIDto][i].Node1 == this.Nodes[objectIDfrom])
				{
					deleted = this.BackEdges[objectIDto][i];
					// Note:  Don't need to remove this child, did it above on the regular edge
					this.BackEdges[objectIDto][i] = this.BackEdges[objectIDto][len - 1];
					len -= 1;
					this.BackEdges[objectIDto].pop();
				}
			}
		}
		return undo;
	}

	this.deleteIncident = function(objectID)
	{
		var undoStack = [];

		if (this.Edges[objectID] != null)
		{
			var len = this.Edges[objectID].length;
			for (var i = len - 1; i >= 0; i--)
			{
				var deleted = this.Edges[objectID][i];
				var node2ID = deleted.Node2.identifier();
				undoStack.push(deleted.createUndoDisconnect());

				var len2 = this.BackEdges[node2ID].length;
				for (var j = len2 - 1; j >=0; j--)
				{
					if (this.BackEdges[node2ID][j] == deleted)
					{
						this.BackEdges[node2ID][j] = this.BackEdges[node2ID][len2 - 1];
						len2 -= 1;
						this.BackEdges[node2ID].pop();
					}
				}
			}
			this.Edges[objectID] = null;
		}
		if (this.BackEdges[objectID] != null)
		{
			len = this.BackEdges[objectID].length;
			for (i = len - 1; i >= 0; i--)
			{
				deleted = this.BackEdges[objectID][i];
				var node1ID = deleted.Node1.identifier();
				undoStack.push(deleted.createUndoDisconnect());

				len2 = this.Edges[node1ID].length;
				for (j = len2 - 1; j >=0; j--)
				{
					if (this.Edges[node1ID][j] == deleted)
					{
						this.Edges[node1ID][j] = this.Edges[node1ID][len2 - 1];
						len2 -= 1;
						this.Edges[node1ID].pop();
					}
				}
			}
			this.BackEdges[objectID] = null;
		}
		return undoStack;
	}


	this.removeObject = function(ObjectID)
	{
		var OldObject = this.Nodes[ObjectID];
		if (ObjectID == this.Nodes.length - 1)
		{
			this.Nodes.pop();
		}
		else
		{
			this.Nodes[ObjectID] = null;
		}
	}

	this.getObject = function(objectID)
	{
		if (this.Nodes[objectID] == null || this.Nodes[objectID] == undefined)
		{
			throw "getObject:Object with ID (" + String(objectID) + ") does not exist"
		}
		return this.Nodes[objectID];

	}


	this.addCircleObject = function (objectID, objectLabel)
	{
			if (this.Nodes[objectID] != null && this.Nodes[objectID] != undefined)
			{
				throw "addCircleObject:Object with same ID (" + String(objectID) + ") already Exists!"
			}
			var newNode = new runtime.AnimatedCircle(objectID, objectLabel);
			this.Nodes[objectID] = newNode;
	}

	this.getNodeX = function(nodeID)
	{
		if (this.Nodes[nodeID] == null || this.Nodes[nodeID] == undefined)
		{
			throw "getting x position of an object that does not exit";
		}
		return this.Nodes[nodeID].x;
	}

	this.getTextWidth = function(text)
	{
		// TODO:  Need to make fonts more flexible, and less hardwired.
		this.ctx.font = '10px sans-serif';
		if (text==undefined)
		{
			w = 3;
		}
		var strList = text.split("\n");
		var width = 0;
		if (strList.length == 1)
		{
 			width = this.ctx.measureText(text).width;
		}
		else
		{
			for (var i = 0; i < strList.length; i++)
			{
				width = Math.max(width, this.ctx.measureText(strList[i]).width);
			}
		}

		return width;
	}

	this.setText = function(nodeID, text, index)
	{
		if (this.Nodes[nodeID] == null || this.Nodes[nodeID] == undefined)
		{
			return;
			throw "setting text of an object that does not exit";
		}
		this.Nodes[nodeID].setText(text, index, this.getTextWidth(text));

	}

	this.getText = function(nodeID, index)
	{
		if (this.Nodes[nodeID] == null || this.Nodes[nodeID] == undefined)
		{
			throw "getting text of an object that does not exit";
		}
		return this.Nodes[nodeID].getText(index);

	}

	this.getNodeY = function(nodeID)
	{
		if (this.Nodes[nodeID] == null || this.Nodes[nodeID] == undefined)
		{
			throw "getting y position of an object that does not exit";
		}
		return this.Nodes[nodeID].y;
	}


	this.connectEdge = function(objectIDfrom, objectIDto, color, curve, directed, lab, connectionPoint)
	{
		var fromObj = this.Nodes[objectIDfrom];
		var toObj = this.Nodes[objectIDto];
		if (fromObj == null || toObj == null)
		{
			throw "Tried to connect two nodes, one didn't exist!";
		}
		var l = new runtime.Line(fromObj,toObj, color, curve, directed, lab, connectionPoint);
		if (this.Edges[objectIDfrom] == null || this.Edges[objectIDfrom] == undefined)
		{
			this.Edges[objectIDfrom] = [];
		}
		if (this.BackEdges[objectIDto] == null || this.BackEdges[objectIDto] == undefined)
		{
			this.BackEdges[objectIDto] = [];
		}
		l.addedToScene = fromObj.addedToScene && toObj.addedToScene;
		this.Edges[objectIDfrom].push(l);
		this.BackEdges[objectIDto].push(l);

	}


	this.setNull = function(objectID, nullVal)
	{
		if (this.Nodes[objectID] != null && this.Nodes[objectID] != undefined)
		{
			this.Nodes[objectID].setNull(nullVal);

		}
	}

	this.getNull = function(objectID)
	{
		if (this.Nodes[objectID] != null && this.Nodes[objectID] != undefined)
		{
			return this.Nodes[objectID].getNull();
		}
		return false;  // TODO:  Error here?
	}



	this.setEdgeColor = function(fromID, toID, color) // returns old color
	{
		var oldColor ="#000000";
		if (this.Edges[fromID] != null &&
			this.Edges[fromID] != undefined)
		{
			var len = this.Edges[fromID].length;
			for (var i = len - 1; i >= 0; i--)
			{
				if (this.Edges[fromID][i] != null &&
					this.Edges[fromID][i] != undefined &&
					this.Edges[fromID][i].Node2 == this.Nodes[toID])
				{
					oldColor = this.Edges[fromID][i].color();
					this.Edges[fromID][i].setColor(color);
				}
			}
		}
		return oldColor;
	}

	this.alignTop = function(id1, id2)
	{
		if (this.Nodes[id1] == null || this.Nodes[id1] == undefined ||
			this.Nodes[id2] == null || this.Nodes[id2] == undefined)
		{
			throw "Tring to align two nodes, one doesn't exist: " + String(id1) + "," + String(id2);
		}
		this.Nodes[id1].alignTop(this.Nodes[id2]);
	}

	this.alignLeft = function(id1, id2)
	{
		if (this.Nodes[id1] == null || this.Nodes[id1] == undefined ||
			this.Nodes[id2] == null || this.Nodes[id2] == undefined)
		{
			throw "Tring to align two nodes, one doesn't exist: " + String(id1) + "," + String(id2);
		}
		this.Nodes[id1].alignLeft(this.Nodes[id2]);
	}

	this.alignRight = function(id1, id2)
	{
		if (this.Nodes[id1] == null || this.Nodes[id1] == undefined ||
			this.Nodes[id2] == null || this.Nodes[id2] == undefined)
		{
			throw "Tring to align two nodes, one doesn't exist: " + String(id1) + "," + String(id2);
		}
		this.Nodes[id1].alignRight(this.Nodes[id2]);
	}



	this.getAlignRightPos = function(id1, id2)
	{
		if (this.Nodes[id1] == null || this.Nodes[id1] == undefined ||
			this.Nodes[id2] == null || this.Nodes[id2] == undefined)
		{
			throw "Tring to align two nodes, one doesn't exist: " + String(id1) + "," + String(id2);
		}
	        return this.Nodes[id1].getAlignRightPos(this.Nodes[id2]);
	}

	this.getAlignLeftPos = function(id1, id2)
	{
		if (this.Nodes[id1] == null || this.Nodes[id1] == undefined ||
			this.Nodes[id2] == null || this.Nodes[id2] == undefined)
		{
			throw "Tring to align two nodes, one doesn't exist: " + String(id1) + "," + String(id2);
		}
	        return this.Nodes[id1].getAlignLeftPos(this.Nodes[id2]);
	}



	this.alignBottom = function(id1, id2)
	{
		if (this.Nodes[id1] == null || this.Nodes[id1] == undefined ||
			this.Nodes[id2] == null || this.Nodes[id2] == undefined)
		{
			throw "Tring to align two nodes, one doesn't exist: " + String(id1) + "," + String(id2);
		}
		this.Nodes[id1].alignBottom(this.Nodes[id2]);
	}


	this.setEdgeHighlight = function(fromID, toID, val) // returns old color
	{
		var oldHighlight = false;
		if (this.Edges[fromID] != null &&
			this.Edges[fromID] != undefined)
		{
			var len = this.Edges[fromID].length;
			for (var i = len - 1; i >= 0; i--)
			{
				if (this.Edges[fromID][i] != null &&
					this.Edges[fromID][i] != undefined &&
					this.Edges[fromID][i].Node2 == this.Nodes[toID])
				{
					oldHighlight = this.Edges[fromID][i].highlighted;
					this.Edges[fromID][i].setHighlight(val);
				}
			}
		}
		return oldHighlight;
	}
	this.addLabelObject = function(objectID, objectLabel, centering)
	{
		if (this.Nodes[objectID] != null && this.Nodes[objectID] != undefined)
		{
			throw new Error("addLabelObject: Object Already Exists!");
		}

		var newLabel = new runtime.AnimatedLabel(objectID, objectLabel, centering, this.getTextWidth(objectLabel));
		this.Nodes[objectID] = newLabel;
	}


	this.addLinkedListObject = function(objectID, nodeLabel, width, height, linkPer, verticalOrientation, linkPosEnd, numLabels, backgroundColor, foregroundColor)
	{
		if (this.Nodes[objectID] != null)
		{
			throw new Error("addLinkedListObject:Object with same ID already Exists!");
			return;
		}
		var newNode  = new runtime.AnimatedLinkedList(objectID, nodeLabel, width, height, linkPer, verticalOrientation, linkPosEnd, numLabels, backgroundColor, foregroundColor);
		this.Nodes[objectID] = newNode;
	}


	this.getNumElements = function(objectID)
	{
		return this.Nodes[objectID].getNumElements();
	}


	this.setNumElements = function(objectID, numElems)
	{
		this.Nodes[objectID].setNumElements(numElems);
	}
	this.addBTreeNode = function(objectID, widthPerElem, height, numElems, backgroundColor, foregroundColor)
	 {
		 backgroundColor = (backgroundColor == undefined) ? "#FFFFFF" : backgroundColor;
		 foregroundColor = (foregroundColor == undefined) ? "#FFFFFF" : foregroundColor;

		 if (this.Nodes[objectID] != null && this.Nodes[objectID] != undefined)
		 {
			 throw "addBTreeNode:Object with same ID already Exists!";
		 }

		 var newNode = new runtime.AnimatedBTreeNode(objectID,widthPerElem, height, numElems, backgroundColor, foregroundColor);
		 this.Nodes[objectID] = newNode;
	 }

	 this.addRectangleObject = function(objectID,nodeLabel, width, height, xJustify , yJustify , backgroundColor, foregroundColor)
	 {
		 if (this.Nodes[objectID] != null || this.Nodes[objectID] != undefined)
		 {
			 throw new Error("addRectangleObject:Object with same ID already Exists!");
		 }
		 var newNode = new runtime.AnimatedRectangle(objectID, nodeLabel, width, height, xJustify, yJustify, backgroundColor, foregroundColor);
		 this.Nodes[objectID] = newNode;

	 }




	this.setNodePosition = function(nodeID, newX, newY)
	{
		if (this.Nodes[nodeID] == null || this.Nodes[nodeID] == undefined)
		{
			// TODO:  Error here?
			return;
		}
		if (newX == undefined || newY == undefined)
		{

			return;
		}
		this.Nodes[nodeID].x = newX;
		this.Nodes[nodeID].y = newY;
		/* Don't need to dirty anything, since we repaint everything every frame
		 (TODO:  Revisit if we do conditional redraws)
		 }*/

	}

}










Object.defineProperties(runtime, {
"ObjectManager": { configurable: true, get: function() { return ObjectManager; }, set: function(value) { ObjectManager = value; } }
});
}


﻿// Copyright 2011 David Galles, University of San Francisco. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
// of conditions and the following disclaimer in the documentation and/or other materials
// provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those of the
// authors and should not be interpreted as representing official policies, either expressed
// or implied, of the University of San Francisco


function installAnimation(runtime) {
var timer;
var animationManager;
var objectManager;
var canvas;
var disposed = false;

function publishState() {
    if (!animationManager || disposed) return;
    runtime.onState({
        running: animationManager.currentlyAnimating,
        waiting: animationManager.awaitingStep,
        paused: animationManager.animationPaused,
        canUndo: animationManager.undoStack.length > 0
    });
}
function timeout() {
    if (disposed) return;
    timer = setTimeout(timeout, 30);
    try {
        animationManager.update();
        objectManager.draw();
    } catch (error) {
        clearTimeout(timer);
        runtime.onError(error);
    }
}
function initCanvas() {
    canvas = runtime.document.getElementById('canvas');
    objectManager = new runtime.ObjectManager();
    animationManager = new AnimationManager(objectManager);
    animationManager.SetSpeed(runtime.speed);
    objectManager.width = canvas.width;
    objectManager.height = canvas.height;
    ['AnimationStarted', 'AnimationEnded', 'AnimationWaiting', 'AnimationUndoUnavailable'].forEach(function(event) {
        animationManager.addListener(event, runtime, function() {
            objectManager.statusReport.setText('');
            publishState();
        });
    });
    return animationManager;
}
function dispose() {
    disposed = true;
    clearTimeout(timer);
}

function AnimationManager(objectManager)
{
	// Holder for all animated objects.
	// All animation is done by manipulating objects in\
	// this container
	this.animatedObjects = objectManager;

	// Control variables for stopping / starting animation

	this.animationPaused = false;
	this.awaitingStep = false;
	this.currentlyAnimating = false;

	// Array holding the code for the animation.  This is
	// an array of strings, each of which is an animation command
	// currentAnimation is an index into this array
	this.AnimationSteps = [];
	this.currentAnimation = 0;

	this.previousAnimationSteps = [];

	// Control variables for where we are in the current animation block.
	//  currFrame holds the frame number of the current animation block,
	//  while animationBlockLength holds the length of the current animation
	//  block (in frame numbers).
	this.currFrame = 0;
	this.animationBlockLength = 0;

	//  The animation block that is currently running.  Array of singleAnimations
	this.currentBlock = null;

	/////////////////////////////////////
	// Variables for handling undo.
	////////////////////////////////////
	//  A stack of UndoBlock objects (subclassed, UndoBlock is an abstract base class)
	//  each of which can undo a single animation element
	this.undoStack = [];
	this.doingUndo = false;

	// A stack containing the beginning of each animation block, as an index
	// into the AnimationSteps array
	this.undoAnimationStepIndices = [];
	this.undoAnimationStepIndicesStack = [];

	this.animationBlockLength = 10;

	this.lerp = function(from, to, percent)
	{
		return (to - from) * percent + from;
	}

	// Pause / unpause animation
	this.SetPaused = function(pausedValue)
	{
		this.animationPaused = pausedValue;
		if (!this.animationPaused)
		{
			this.step();
		}
	}

	// Set the speed of the animation, from 0 (slow) to 100 (fast)
	this.SetSpeed = function(newSpeed)
	{
		this.animationBlockLength = Math.floor((100-newSpeed) / 2);
	}


	this.parseBool = function(str)
	{
		var uppercase = str.toUpperCase();
		var returnVal =  !(uppercase == "False" || uppercase == "f" || uppercase == " 0" || uppercase == "0" || uppercase == "");
		return returnVal;

	}

	this.parseColor = function(clr)
	{
			if (clr.charAt(0) == "#")
			{
				return clr;
			}
			else if (clr.substring(0,2) == "0x")
			{
				return "#" + clr.substring(2);
			}
	}


	this.startNextBlock = function()
	{
		this.awaitingStep = false;
		this.currentBlock = [];
		var undoBlock = []
		if (this.currentAnimation == this.AnimationSteps.length )
		{
			this.currentlyAnimating = false;
			this.awaitingStep = false;
			this.fireEvent("AnimationEnded","NoData");
			clearTimeout(timer);
			this.animatedObjects.update();
			this.animatedObjects.draw();

			return;
		}
		this.undoAnimationStepIndices.push(this.currentAnimation);

		var foundBreak= false;
		var anyAnimations= false;

		while (this.currentAnimation < this.AnimationSteps.length && !foundBreak)
		{
			var nextCommand = this.AnimationSteps[this.currentAnimation].split("<;>");
			if (nextCommand[0].toUpperCase() == "CREATECIRCLE")
			{
				this.animatedObjects.addCircleObject(parseInt(nextCommand[1]), nextCommand[2]);
				if (nextCommand.length > 4)
				{
					this.animatedObjects.setNodePosition(parseInt(nextCommand[1]), parseInt(nextCommand[3]), parseInt(nextCommand[4]));
				}
				undoBlock.push(new runtime.UndoCreate(parseInt(nextCommand[1])));

			}
			else if (nextCommand[0].toUpperCase() == "CONNECT")
			{

				if (nextCommand.length > 7)
				{
					this.animatedObjects.connectEdge(parseInt(nextCommand[1]),
                                                                         parseInt(nextCommand[2]),
                                                                         this.parseColor(nextCommand[3]),
                                                                         parseFloat(nextCommand[4]),
                                                                         this.parseBool(nextCommand[5]),
                                                                         nextCommand[6],
                                                                         parseInt(nextCommand[7]));
				}
				else if (nextCommand.length > 6)
				{
					this.animatedObjects.connectEdge(parseInt(nextCommand[1]),
                                                                         parseInt(nextCommand[2]),
                                                                         this.parseColor(nextCommand[3]),
                                                                         parseFloat(nextCommand[4]),
                                                                         this.parseBool(nextCommand[5]),
                                                                         nextCommand[6],
                                                                         0);
				}
				else if (nextCommand.length > 5)
				{
					this.animatedObjects.connectEdge(parseInt(nextCommand[1]),
                                                                         parseInt(nextCommand[2]),
                                                                         this.parseColor(nextCommand[3]),
                                                                         parseFloat(nextCommand[4]),
                                                                         this.parseBool(nextCommand[5]),
                                                                         "",
                                                                         0);
				}
				else if (nextCommand.length > 4)
				{
					this.animatedObjects.connectEdge(parseInt(nextCommand[1]),
                                                                         parseInt(nextCommand[2]),
                                                                         this.parseColor(nextCommand[3]),
                                                                         parseFloat(nextCommand[4]),
                                                                         true,
                                                                         "",
                                                                         0);
				}
				else if (nextCommand.length > 3)
				{
					this.animatedObjects.connectEdge(parseInt(nextCommand[1]),
                                                                         parseInt(nextCommand[2]),
																		 this.parseColor(nextCommand[3]),
                                                                         0.0,
                                                                         true,
                                                                         "",
                                                                         0);
				}
				else
				{
					this.animatedObjects.connectEdge(parseInt(nextCommand[1]),
                                                                         parseInt(nextCommand[2]),
													                    "#000000",
                                                                         0.0,
                                                                         true,
                                                                         "",
                                                                         0);

				}
				undoBlock.push(new runtime.UndoConnect(parseInt(nextCommand[1]), parseInt (nextCommand[2]), false));
			}
			else if (nextCommand[0].toUpperCase() == "CREATERECTANGLE")
			{
				if (nextCommand.length == 9)
				{
					this.animatedObjects.addRectangleObject(parseInt(nextCommand[1]), // ID
															nextCommand[2], // Label
															parseInt(nextCommand[3]), // w
															parseInt(nextCommand[4]), // h
															nextCommand[7], // xJustify
															nextCommand[8],// yJustify
															"#ffffff", // background color
					                                        "#000000"); // foreground color
				}
				else
				{
					this.animatedObjects.addRectangleObject(parseInt(nextCommand[1]), // ID
															nextCommand[2], // Label
															parseInt(nextCommand[3]), // w
															parseInt(nextCommand[4]), // h
															"center", // xJustify
															"center",// yJustify
															"#ffffff", // background color
					                                        "#000000"); // foreground color

				}
				if (nextCommand.length > 6)
				{
					this.animatedObjects.setNodePosition(parseInt(nextCommand[1]), parseInt(nextCommand[5]), parseInt(nextCommand[6]));
				}
				undoBlock.push(new runtime.UndoCreate(parseInt(nextCommand[1])));
			}

			else if (nextCommand[0].toUpperCase() == "MOVE")
			{
				var objectID = parseInt(nextCommand[1]);
				var nextAnim =  new SingleAnimation(objectID,
													this.animatedObjects.getNodeX(objectID),
													this.animatedObjects.getNodeY(objectID),
													parseInt(nextCommand[2]),
													parseInt(nextCommand[3]));
				this.currentBlock.push(nextAnim);

				undoBlock.push(new runtime.UndoMove(nextAnim.objectID, nextAnim.toX, nextAnim.toY, nextAnim.fromX, nextAnim.fromY));

				anyAnimations = true;
			}

			else if (nextCommand[0].toUpperCase() == "MOVETOALIGNRIGHT")
			{
				var id = parseInt(nextCommand[1]);
				var otherId = parseInt(nextCommand[2]);
                                var newXY = this.animatedObjects.getAlignRightPos(id, otherId);


				var nextAnim =  new SingleAnimation(id,
								    this.animatedObjects.getNodeX(id),
								    this.animatedObjects.getNodeY(id),
								    newXY[0],
								    newXY[1]);
				this.currentBlock.push(nextAnim);
				undoBlock.push(new runtime.UndoMove(nextAnim.objectID, nextAnim.toX, nextAnim.toY, nextAnim.fromX, nextAnim.fromY));
				anyAnimations = true;
			}

			else if (nextCommand[0].toUpperCase() == "STEP")
			{
				foundBreak = true;
			}
			else if (nextCommand[0].toUpperCase() == "SETFOREGROUNDCOLOR")
			{
				var id = parseInt(nextCommand[1]);
				var oldColor = this.animatedObjects.foregroundColor(id);
				this.animatedObjects.setForegroundColor(id, this.parseColor(nextCommand[2]));
				undoBlock.push(new runtime.UndoSetForegroundColor(id, oldColor));
			}
			else if (nextCommand[0].toUpperCase() == "SETBACKGROUNDCOLOR")
			{
				id = parseInt(nextCommand[1]);
				oldColor = this.animatedObjects.backgroundColor(id);
				this.animatedObjects.setBackgroundColor(id, this.parseColor(nextCommand[2]));
				undoBlock.push(new runtime.UndoSetBackgroundColor(id, oldColor));
			}
			else if (nextCommand[0].toUpperCase() == "SETHIGHLIGHT")
			{
				var newHighlight = this.parseBool(nextCommand[2]);
				this.animatedObjects.setHighlight( parseInt(nextCommand[1]), newHighlight);
				undoBlock.push(new runtime.UndoHighlight( parseInt(nextCommand[1]), !newHighlight));
			}
			else if (nextCommand[0].toUpperCase() == "DISCONNECT")
			{
				var undoConnect = this.animatedObjects.disconnect(parseInt(nextCommand[1]), parseInt(nextCommand[2]));
				if (undoConnect != null)
				{
					undoBlock.push(undoConnect);
				}
			}
			else if (nextCommand[0].toUpperCase() == "SETALPHA")
			{
				var oldAlpha = this.animatedObjects.getAlpha(parseInt(nextCommand[1]));
				this.animatedObjects.setAlpha(parseInt(nextCommand[1]), parseFloat(nextCommand[2]));
				undoBlock.push(new runtime.UndoSetAlpha(parseInt(nextCommand[1]), oldAlpha));
			}
			else if (nextCommand[0].toUpperCase() == "SETTEXT")
			{
				if (nextCommand.length > 3)
				{
					var oldText = this.animatedObjects.getText(parseInt(nextCommand[1]), parseInt(nextCommand[3]));
					this.animatedObjects.setText(parseInt(nextCommand[1]), nextCommand[2], parseInt(nextCommand[3]));
					if (oldText != undefined)
					{
						undoBlock.push(new runtime.UndoSetText(parseInt(nextCommand[1]), oldText, parseInt(nextCommand[3]) ));
					}
				}
				else
				{
					oldText = this.animatedObjects.getText(parseInt(nextCommand[1]), 0);
					this.animatedObjects.setText(parseInt(nextCommand[1]), nextCommand[2], 0);
					if (oldText != undefined)
					{
						undoBlock.push(new runtime.UndoSetText(parseInt(nextCommand[1]), oldText, 0));
					}
				}
			}
			else if (nextCommand[0].toUpperCase() == "DELETE")
			{
				var objectID  = parseInt(nextCommand[1]);

				var i;
				var removedEdges = this.animatedObjects.deleteIncident(objectID);
				if (removedEdges.length > 0)
				{
					undoBlock = undoBlock.concat(removedEdges);
				}
				var obj = this.animatedObjects.getObject(objectID);
				if (obj != null)
				{
					undoBlock.push(obj.createUndoDelete());
					this.animatedObjects.removeObject(objectID);
				}
			}
			else if (nextCommand[0].toUpperCase() == "CREATEHIGHLIGHTCIRCLE")
			{
				if (nextCommand.length > 5)
				{
					this.animatedObjects.addHighlightCircleObject(parseInt(nextCommand[1]), this.parseColor(nextCommand[2]), parseFloat(nextCommand[5]));
				}
				else
				{
					this.animatedObjects.addHighlightCircleObject(parseInt(nextCommand[1]), this.parseColor(nextCommand[2]), 20);
				}
				if (nextCommand.length > 4)
				{
					this.animatedObjects.setNodePosition(parseInt(nextCommand[1]), parseInt(nextCommand[3]), parseInt(nextCommand[4]));
				}
				undoBlock.push(new runtime.UndoCreate(parseInt(nextCommand[1])));


			}
			else if (nextCommand[0].toUpperCase() == "CREATELABEL")
			{
				if (nextCommand.length == 6)
				{
					this.animatedObjects.addLabelObject(parseInt(nextCommand[1]), nextCommand[2], this.parseBool(nextCommand[5]));
				}
				else
				{
					this.animatedObjects.addLabelObject(parseInt(nextCommand[1]), nextCommand[2], true);
				}
				if (nextCommand.length >= 5)
				{

					this.animatedObjects.setNodePosition(parseInt(nextCommand[1]), parseFloat(nextCommand[3]), parseFloat(nextCommand[4]));
				}
				undoBlock.push(new runtime.UndoCreate(parseInt(nextCommand[1])));
			}
			else if (nextCommand[0].toUpperCase() == "SETEDGECOLOR")
			{
				var from = parseInt(nextCommand[1]);
				var to = parseInt(nextCommand[2]);
				var newColor = this.parseColor(nextCommand[3]);
				var oldColor = this.animatedObjects.setEdgeColor(from, to, newColor);
				undoBlock.push(new runtime.UndoSetEdgeColor(from, to, oldColor));
			}
			else if (nextCommand[0].toUpperCase() == "SETEDGEALPHA")
			{
				var from = parseInt(nextCommand[1]);
				var to = parseInt(nextCommand[2]);
				var newAlpha = parseFloat(nextCommand[3]);
				var oldAplpha = this.animatedObjects.setEdgeAlpha(from, to, newAlpha);
				undoBlock.push(new runtime.UndoSetEdgeAlpha(from, to, oldAplpha));
			}


			else if (nextCommand[0].toUpperCase() == "SETEDGEHIGHLIGHT")
			{
				var newHighlight = this.parseBool(nextCommand[3]);
				var from = parseInt(nextCommand[1]);
				var to = parseInt(nextCommand[2]);
				var oldHighlight = this.animatedObjects.setEdgeHighlight(from, to, newHighlight);
				undoBlock.push(new runtime.UndoHighlightEdge(from, to, oldHighlight));
			}
			else if (nextCommand[0].toUpperCase() == "SETHEIGHT")
			{
				id = parseInt(nextCommand[1]);
				var oldHeight = this.animatedObjects.getHeight(id);
				this.animatedObjects.setHeight(id, parseInt(nextCommand[2]));
				undoBlock.push(new runtime.UndoSetHeight(id, oldHeight));
			}
			else if (nextCommand[0].toUpperCase() == "SETLAYER")
			{
				this.animatedObjects.setLayer(parseInt(nextCommand[1]), parseInt(nextCommand[2]));
				//TODO: Add undo information here
			}


			else if (nextCommand[0].toUpperCase() == "CREATELINKEDLIST")
			{
				if (nextCommand.length == 11)
				{
					this.animatedObjects.addLinkedListObject(parseInt(nextCommand[1]), nextCommand[2],
			               parseInt(nextCommand[3]), parseInt(nextCommand[4]), parseFloat(nextCommand[7]),
			               this.parseBool(nextCommand[8]), this.parseBool(nextCommand[9]),parseInt(nextCommand[10]), "#FFFFFF", "#000000");
				}
				else
				{
					this.animatedObjects.addLinkedListObject(parseInt(nextCommand[1]), nextCommand[2], parseInt(nextCommand[3]), parseInt(nextCommand[4]), 0.25, true, false, 1, "#FFFFFF", "#000000");
				}
				if (nextCommand.length > 6)
				{
					this.animatedObjects.setNodePosition(parseInt(nextCommand[1]), parseInt(nextCommand[5]), parseInt(nextCommand[6]));
					undoBlock.push(new runtime.UndoCreate(parseInt(nextCommand[1])));
				}

			}
			else if (nextCommand[0].toUpperCase() == "SETNULL")
			{
				var oldNull = this.animatedObjects.getNull(parseInt(nextCommand[1]));
				this.animatedObjects.setNull(parseInt(nextCommand[1]), this.parseBool(nextCommand[2]));
				undoBlock.push(new runtime.UndoSetNull(parseInt(nextCommand[1]), oldNull));
			}
			else if (nextCommand[0].toUpperCase() == "SETTEXTCOLOR")
			{
				if (nextCommand.length > 3)
				{
					oldColor = this.animatedObjects.getTextColor(parseInt(nextCommand[1]), parseInt(nextCommand[3]));
					this.animatedObjects.setTextColor(parseInt(nextCommand[1]), this.parseColor(nextCommand[2]), parseInt(nextCommand[3]));
					undoBlock.push(new runtime.UndoSetTextColor(parseInt(nextCommand[1]), oldColor, parseInt(nextCommand[3]) ));
				}
				else
				{
					oldColor = this.animatedObjects.getTextColor(parseInt(nextCommand[1]), 0);
					this.animatedObjects.setTextColor(parseInt(nextCommand[1]),this.parseColor(nextCommand[2]), 0);
					undoBlock.push(new runtime.UndoSetTextColor(parseInt(nextCommand[1]), oldColor, 0));
				}
			}


			else if (nextCommand[0].toUpperCase() == "CREATEBTREENODE")
			{

				this.animatedObjects.addBTreeNode(parseInt(nextCommand[1]), parseFloat(nextCommand[2]), parseFloat(nextCommand[3]),
			                 parseInt(nextCommand[4]),this.parseColor(nextCommand[7]), this.parseColor(nextCommand[8]));
				this.animatedObjects.setNodePosition(parseInt(nextCommand[1]), parseInt(nextCommand[5]), parseInt(nextCommand[6]));
				undoBlock.push(new runtime.UndoCreate(parseInt(nextCommand[1])));
			}

			else if (nextCommand[0].toUpperCase() == "SETWIDTH")
			{
				var id = parseInt(nextCommand[1]);
				this.animatedObjects.setWidth(id, parseInt(nextCommand[2]));
				var oldWidth = this.animatedObjects.getWidth(id);
				undoBlock.push(new runtime.UndoSetWidth(id, oldWidth));
			}
			else if (nextCommand[0].toUpperCase() == "SETNUMELEMENTS")
			{
				var oldElem = this.animatedObjects.getObject(parseInt(nextCommand[1]));
				undoBlock.push(new runtime.UndoSetNumElements(oldElem, parseInt(nextCommand[2])));
				this.animatedObjects.setNumElements(parseInt(nextCommand[1]), parseInt(nextCommand[2]));
			}
			else if (nextCommand[0].toUpperCase() == "SETPOSITION")
			{
				var id = parseInt(nextCommand[1])
				var oldX = this.animatedObjects.getNodeX(id);
				var oldY = this.animatedObjects.getNodeY(id);
				undoBlock.push(new runtime.UndoSetPosition(id, oldX, oldY));
				this.animatedObjects.setNodePosition(id, parseInt(nextCommand[2]), parseInt(nextCommand[3]));
			}
			else if (nextCommand[0].toUpperCase() == "ALIGNRIGHT")
			{
				var id = parseInt(nextCommand[1])
				var oldX = this.animatedObjects.getNodeX(id);
				var oldY = this.animatedObjects.getNodeY(id);
				undoBlock.push(new runtime.UndoSetPosition(id, oldX. oldY));
				this.animatedObjects.alignRight(id, parseInt(nextCommand[2]));
			}
			else if (nextCommand[0].toUpperCase() == "ALIGNLEFT")
			{
				var id = parseInt(nextCommand[1])
				var oldX = this.animatedObjects.getNodeX(id);
				var oldY = this.animatedObjects.getNodeY(id);
				undoBlock.push(new runtime.UndoSetPosition(id, oldX. oldY));
				this.animatedObjects.alignLeft(id, parseInt(nextCommand[2]));
			}
			else if (nextCommand[0].toUpperCase() == "ALIGNTOP")
			{
				var id = parseInt(nextCommand[1])
				var oldX = this.animatedObjects.getNodeX(id);
				var oldY = this.animatedObjects.getNodeY(id);
				undoBlock.push(new runtime.UndoSetPosition(id, oldX. oldY));
				this.animatedObjects.alignTop(id, parseInt(nextCommand[2]));
			}
			else if (nextCommand[0].toUpperCase() == "ALIGNBOTTOM")
			{
				var id = parseInt(nextCommand[1])
				var oldX = this.animatedObjects.getNodeX(id);
				var oldY = this.animatedObjects.getNodeY(id);
				undoBlock.push(new runtime.UndoSetPosition(id, oldX. oldY));
				this.animatedObjects.alignBottom(id, parseInt(nextCommand[2]));
			}





			else if (nextCommand[0].toUpperCase() == "SETHIGHLIGHTINDEX")
			{
				var id = parseInt(nextCommand[1]);
				var index = parseInt(nextCommand[2]);
                                var oldIndex = this.animatedObjects.getHighlightIndex(id)
				undoBlock.push(new runtime.UndoSetHighlightIndex(id, oldIndex));
				this.animatedObjects.setHighlightIndex(id,index);
			}
			else
			{
	//			throw "Unknown command: " + nextCommand[0];
			}

			this.currentAnimation = this.currentAnimation+1;
		}
		this.currFrame = 0;

		// Hack:  If there are not any animations, and we are currently paused,
		// then set the current frame to the end of the anumation, so that we will
		// advance immediagely upon the next step button.  If we are not paused, then
		// animate as normal.

		if (!anyAnimations && this.animationPaused || (!anyAnimations && this.currentAnimation == this.AnimationSteps.length) )
		{
			this.currFrame = this.animationBlockLength;
		}

		this.undoStack.push(undoBlock);
	}

	//  Start a new animation.  The input parameter commands is an array of strings,
	//  which represents the animation to start
	this.StartNewAnimation =  function(commands)
	{
		clearTimeout(timer);
		if (this.AnimationSteps != null)
		{
			this.previousAnimationSteps.push(this.AnimationSteps);
			this.undoAnimationStepIndicesStack.push(this.undoAnimationStepIndices);
		}
		if (commands == undefined || commands.length == 0)
		{
			this.AnimationSteps = ["Step"];
		}
		else
		{
			this.AnimationSteps = commands;
		}
		this.undoAnimationStepIndices = new Array();
		this.currentAnimation = 0;
		this.startNextBlock();
		this.currentlyAnimating = true;
		this.fireEvent("AnimationStarted","NoData");
		timer = setTimeout(timeout, 30);

	}


	// Step backwards one step.  A no-op if the animation is not currently paused
	this.stepBack = function()
	{
		if (this.awaitingStep && this.undoStack != null && this.undoStack.length != 0)
		{
			//  TODO:  Get events working correctly!
			this.fireEvent("AnimationStarted","NoData");
			clearTimeout(timer);

			this.awaitingStep = false;
			this.undoLastBlock();
			// Re-kick thie timer.  The timer may or may not be running at this point,
			// so to be safe we'll kill it and start it again.
			clearTimeout(timer);
			timer = setTimeout(timeout, 30);


		}
		else if (!this.currentlyAnimating && this.animationPaused && this.undoAnimationStepIndices != null)
		{
			this.fireEvent("AnimationStarted","NoData");
			this.currentlyAnimating = true;
			this.undoLastBlock();
			// Re-kick thie timer.  The timer may or may not be running at this point,
			// so to be safe we'll kill it and start it again.
			clearTimeout(timer);
			timer = setTimeout(timeout, 30);

		}

	}
	// Step forwards one step.  A no-op if the animation is not currently paused
	this.step = function()
	{
		if (this.awaitingStep)
		{
			this.startNextBlock();
			this.fireEvent("AnimationStarted","NoData");
			this.currentlyAnimating = true;
			// Re-kick thie timer.  The timer should be going now, but we've had some difficulty with
			// it timing itself out, so we'll be safe and kick it now.
			clearTimeout(timer);
			timer = setTimeout(timeout, 30);
		}
	}


	/// WARNING:  Could be dangerous to call while an animation is running ...
	this.clearHistory = function()
	{
		this.undoStack = [];
		this.undoAnimationStepIndices = null;
		this.previousAnimationSteps = [];
		this.undoAnimationStepIndicesStack = [];
		this.AnimationSteps = null;
		this.fireEvent("AnimationUndoUnavailable","NoData");
		clearTimeout(timer);
		this.animatedObjects.update();
		this.animatedObjects.draw();

	}

	this.skipBack = function()
	{
		var keepUndoing = this.undoAnimationStepIndices != null && this. undoAnimationStepIndices.length != 0;
		if (keepUndoing)
		{
			var i;
			for (i = 0; this.currentBlock != null && i < this.currentBlock.length; i++)
			{
				var objectID = this.currentBlock[i].objectID;
				this.animatedObjects.setNodePosition(objectID,
												this.currentBlock[i].toX,
												this.currentBlock[i].toY);
			}
			if (this.doingUndo)
			{
				this.finishUndoBlock(this.undoStack.pop())
			}
			while (keepUndoing)
			{
				this.undoLastBlock();
				for (i = 0; i < this.currentBlock.length; i++)
				{
					objectID = this.currentBlock[i].objectID;
					this.animatedObjects.setNodePosition(objectID,
													this.currentBlock[i].toX,
													this.currentBlock[i].toY);
				}
				keepUndoing = this.finishUndoBlock(this.undoStack.pop());

			}
			clearTimeout(timer);
			this.animatedObjects.update();
			this.animatedObjects.draw();
			if (this.undoStack == null || this.undoStack.length == 0)
			{
				this.fireEvent("AnimationUndoUnavailable","NoData");
			}

		}
	}

	this.resetAll = function()
	{
		this.clearHistory();
		this.animatedObjects.clearAllObjects();
		this.animatedObjects.draw();
		clearTimeout(timer);
	}

	this.skipForward = function()
	{
		if (this.currentlyAnimating)
		{
			this.animatedObjects.runFast = true;
			while (this.AnimationSteps != null && this.currentAnimation < this.AnimationSteps.length)
			{
				var i;
				for (i = 0; this.currentBlock != null && i < this.currentBlock.length; i++)
				{
					var objectID = this.currentBlock[i].objectID;
					this.animatedObjects.setNodePosition(objectID,
													this.currentBlock[i].toX,
													this.currentBlock[i].toY);
				}
				if (this.doingUndo)
				{
					this.finishUndoBlock(this.undoStack.pop())
				}
				this.startNextBlock();
				for (i= 0; i < this.currentBlock.length; i++)
				{
					var objectID = this.currentBlock[i].objectID;
					this.animatedObjects.setNodePosition(objectID,
													this.currentBlock[i].toX,
													this.currentBlock[i].toY);
				}

			}
			this.animatedObjects.update();
			this.currentlyAnimating = false;
			this.awaitingStep = false;
			this.doingUndo = false;

			this.animatedObjects.runFast = false;
			this.fireEvent("AnimationEnded","NoData");
			clearTimeout(timer);
			this.animatedObjects.update();
			this.animatedObjects.draw();
		}
	}


	this.finishUndoBlock = function(undoBlock)
	{
		for (var i = undoBlock.length - 1; i >= 0; i--)
		{
			undoBlock[i].undoInitialStep(this.animatedObjects);

		}
		this.doingUndo = false;

		// If we are at the final end of the animation ...
		if (this.undoAnimationStepIndices.length == 0)
		{
			this.awaitingStep = false;
			this.currentlyAnimating = false;
			this.undoAnimationStepIndices = this.undoAnimationStepIndicesStack.pop();
			this.AnimationSteps = this.previousAnimationSteps.pop();
			this.fireEvent("AnimationEnded","NoData");
			this.fireEvent("AnimationUndo","NoData");
			this.currentBlock = [];
			if (this.undoStack == null || this.undoStack.length == 0)
			{
				this.currentlyAnimating = false;
				this.awaitingStep = false;
				this.fireEvent("AnimationUndoUnavailable","NoData");
			}

			clearTimeout(timer);
			this.animatedObjects.update();
			this.animatedObjects.draw();


			return false;
		}
		return true;
	}


	this.undoLastBlock = function()
	{

		if (this.undoAnimationStepIndices.length == 0)
		{

			// Nothing on the undo stack.  Return
			return;

		}
		if (this.undoAnimationStepIndices.length > 0)
		{
			this.doingUndo = true;
			var anyAnimations = false;
			this.currentAnimation = this.undoAnimationStepIndices.pop();
			this.currentBlock = [];
			var undo = this.undoStack[this.undoStack.length - 1];
			var i;
			for (i = undo.length - 1; i >= 0; i--)
			{
				var animateNext  =  undo[i].addUndoAnimation(this.currentBlock);
				anyAnimations = anyAnimations || animateNext;

			}
			this.currFrame = 0;

			// Hack:  If there are not any animations, and we are currently paused,
			// then set the current frame to the end of the animation, so that we will
			// advance immediagely upon the next step button.  If we are not paused, then
			// animate as normal.
			if (!anyAnimations && this.animationPaused  )
			{
				this.currFrame = this.animationBlockLength;
			}
			this.currentlyAnimating = true;
		}

	}
	this.setLayer = function(shown, layers)
	{
		this.animatedObjects.setLayer(shown, layers)
		// Drop in an extra draw call here, just in case we are not
		// in the middle of an update loop when this changes
		this.animatedObjects.draw();
	}


	this.setAllLayers = function(layers)
	{
		this.animatedObjects.setAllLayers(layers);
		// Drop in an extra draw call here, just in case we are not
		// in the middle of an update loop when this changes
		this.animatedObjects.draw();
	}


	this.update = function()
	{

		if (this.currentlyAnimating)
		{
			this.currFrame = this.currFrame + 1;
			var i;
			for (i = 0; i < this.currentBlock.length; i++)
			{
				if (this.currFrame == this.animationBlockLength || (this.currFrame == 1 && this.animationBlockLength == 0))
				{
					this.animatedObjects.setNodePosition(this.currentBlock[i].objectID,
													     this.currentBlock[i].toX,
													     this.currentBlock[i].toY);
				}
				else if (this.currFrame < this.animationBlockLength)
				{
					var objectID = this.currentBlock[i].objectID;
					var percent = 1 / (this.animationBlockLength - this.currFrame);
					var oldX = this.animatedObjects.getNodeX(objectID);
					var oldY = this.animatedObjects.getNodeY(objectID);
					var targetX = this.currentBlock[i].toX;
					var targety  = this.currentBlock[i].toY;
					var newX = this.lerp(this.animatedObjects.getNodeX(objectID), this.currentBlock[i].toX, percent);
					var newY = this.lerp(this.animatedObjects.getNodeY(objectID), this.currentBlock[i].toY, percent);
					this.animatedObjects.setNodePosition(objectID, newX, newY);
				}
			}
			if (this.currFrame >= this.animationBlockLength)
			{
				if (this.doingUndo)
				{
					if (this.finishUndoBlock(this.undoStack.pop()))
					{
						this.awaitingStep = true;
						this.fireEvent("AnimationWaiting","NoData");
					}

				}
				else
				{
					if (this.animationPaused && (this.currentAnimation < this.AnimationSteps.length))
					{
						this.awaitingStep = true;
						this.fireEvent("AnimationWaiting","NoData");
						this.currentBlock = [];
					}
					else
					{
						this.startNextBlock();
					}
				}
			}
			this.animatedObjects.update();

		}


	}

}

AnimationManager.prototype = new runtime.EventListener();
AnimationManager.prototype.constructor = AnimationManager;


function SingleAnimation(id, fromX, fromY, toX, toY)
{
	this.objectID = id;
	this.fromX = fromX;
	this.fromY = fromY;
	this.toX = toX;
	this.toY = toY;
}

Object.defineProperties(runtime, {
"timer": { configurable: true, get: function() { return timer; }, set: function(value) { timer = value; } },
"animationManager": { configurable: true, get: function() { return animationManager; }, set: function(value) { animationManager = value; } },
"objectManager": { configurable: true, get: function() { return objectManager; }, set: function(value) { objectManager = value; } },
"canvas": { configurable: true, get: function() { return canvas; }, set: function(value) { canvas = value; } },
"disposed": { configurable: true, get: function() { return disposed; }, set: function(value) { disposed = value; } },
"publishState": { configurable: true, get: function() { return publishState; }, set: function(value) { publishState = value; } },
"timeout": { configurable: true, get: function() { return timeout; }, set: function(value) { timeout = value; } },
"initCanvas": { configurable: true, get: function() { return initCanvas; }, set: function(value) { initCanvas = value; } },
"dispose": { configurable: true, get: function() { return dispose; }, set: function(value) { dispose = value; } },
"AnimationManager": { configurable: true, get: function() { return AnimationManager; }, set: function(value) { AnimationManager = value; } },
"SingleAnimation": { configurable: true, get: function() { return SingleAnimation; }, set: function(value) { SingleAnimation = value; } }
});
}

export default function installAnimations(runtime) {
    [installEvents, installUndo, installObject, installLabel, installCircle, installRectangle, installLinkedList, installBTreeNode, installHighlight, installLine, installObjects, installAnimation].forEach(install => install(runtime));
}
