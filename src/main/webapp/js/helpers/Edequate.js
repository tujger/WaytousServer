/**
 * Edequate - javascript DOM and interface routines
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * History:
 * 1.3 - sprintf redesigned; table#options.sort=true/false; table#options.filter=true/false;
 *       dialog#options.autoclose=true/false; dialog#setHeader; dialog#getHeader;
 *       dialog#setFooter; dialog#getFooter; dialog#setPositive; dialog#getPositive;
 *       dialog#setNeutral; dialog#getNeutral; dialog#setNegative; dialog#getNegative;
 *       menu; create#options.children; create#options.variable; create#options.childName
 * 1.2 - HTMLElement#updateHTML(text)
 * 1.1 - some fixes and improvements
 * 1 - initial release
 */

function Edequate(options) {
    var self = this;

    this.version = 1;

    var HTML = {
        DIV: "div",
        LINK:"link",
        A:"a",
        IMG:"img",
        META:"meta",
        STYLE:"style",
        CLASS:"className",
        CLASSNAME:"className",
        SCRIPT:"script",
        TITLE:"title",
        ID:"id",
        SRC:"src",
        HTTP_EQUIV: "http-equiv",
        TABLE:"table",
        TR:"tr",
        TH:"th",
        TD:"td",
        H1:"h1",
        H2:"h2",
        H3:"h3",
        H4:"h4",
        H5:"h5",
        H6:"h6",
        H7:"h7",
        I:"i",
        BORDER:"border",
        COLSPAN:"colspan",
        ROWSPAN:"rowspan",
        HREF:"href",
        TARGET:"target",
        SMALL:"small",
        REL:"rel",
        STYLESHEET:"stylesheet",
        TYPE:"type",
        BR:"br",
        FORM:"form",
        NAME:"name",
        LABEL:"label",
        INPUT:"input",
        CHECKBOX:"checkbox",
        TEXT:"text",
        NUMBER:"number",
        TEXTAREA:"textarea",
        HIDDEN:"hidden",
        PASSWORD:"password",
        SELECT:"select",
        OPTION:"option",
        SUBMIT:"submit",
        VALUE:"value",
        MANIFEST:"manifest",
        SPAN:"span",
        BUTTON:"button",
        CLICK:"click",
        SVG:"svg",
        PATH:"path",
        MOUSEOVER:"mouseover",
        MOUSEOUT:"mouseout",
        MOUSEUP:"mouseup",
        MOUSEDOWN:"mousedown",
        MOUSEMOVE:"mousemove",
        MOUSEENTER:"mouseenter",
        MOUSELEAVE:"mouseleave",
        VIEWBOX:"viewBox",
        INNERHTML:"innerHTML",
        INNERTEXT:"innerText",
        BLOCK:"block",
        AUTO:"auto",
        AUDIO:"audio"
    };
    this.HTML = HTML;

    var ERRORS = {
        NOT_EXISTS: 1,
        NOT_AN_OBJECT: 2,
        INCORRECT_JSON: 4,
        ERROR_LOADING: 8,
        ERROR_SENDING_REQUEST: 16,
        INVALID_MODULE: 32,
    };
    this.ERRORS = ERRORS;

    var DRAWER = {
        SECTION_PRIMARY: 0,
        SECTION_COMMUNICATION: 2,
        SECTION_SHARE: 3,
        SECTION_NAVIGATION: 5,
        SECTION_VIEWS: 6,
        SECTION_MAP: 7,
        SECTION_MISCELLANEOUS: 8,
        SECTION_LAST: 9
    };
    this.DRAWER = DRAWER;

    var HIDING = {
        OPACITY: "opacity",
        SCALE_XY: "scale-xy",
        SCALE_X: "scale-x",
        SCALE_X_LEFT: "scale-x-left",
        SCALE_X_RIGHT: "scale-x-right",
        SCALE_Y: "scale-y",
        SCALE_Y_TOP: "scale-y-top",
        SCALE_Y_BOTTOM: "scale-y-bottom",
    };
    this.HIDING = HIDING;

    URL = function(link) {
        this.href = link;
        var p = link.split("://");
        this.protocol = "http:";
        if(p.length > 1) this.protocol = p.shift() +":";
        p = p.join("//").split("/");
        this.host = p.shift();
        this.pathname = "/" + p.join("/");
        p = this.host.split(":");
        this.hostname = p.shift();
        this.port = p.shift();
        if(!this.port) this.port = "";
        this.origin = this.protocol + "//" + this.host;
        this.hash = "";
        this.password = "";
        this.search = "";
        this.username = "";

    };

    HTMLElement.prototype.show = function(animatedType) {
        var div = this, parent, holder, computedStyle;
        if(!div.classList.contains("hidden")) return;
        clearTimeout(div.hideTask);
        div.isHidden = false;
        if(animatedType) {
            var height,width;
            switch(animatedType) {
                case HIDING.SCALE_Y:
                case HIDING.SCALE_Y_TOP:
                case HIDING.SCALE_Y_BOTTOM:
                    parent = div.parentNode;
                    holder = create(HTML.DIV, {style:{display:"none"}});
                    parent.replaceChild(holder,div);
                    document.body.appendChild(div);
                    div.style.position = "fixed";
                    div.style.left = "-10000px";
                    div.classList.remove("hidden");

                    computedStyle = window.getComputedStyle(div,null);
                    height = computedStyle.height;

                    div.classList.add("hidden");
                    div.style.position = "";
                    div.style.left = "";
                    parent.replaceChild(div,holder);
                    holder = null;

                    div.style.height = "0px";
                    break;
                case HIDING.SCALE_X:
                case HIDING.SCALE_X_LEFT:
                case HIDING.SCALE_X_RIGHT:
                    parent = div.parentNode;
                    holder = create(HTML.DIV, {style:{display:"none"}});
                    parent.replaceChild(holder,div);
                    document.body.appendChild(div);
                    div.style.position = "fixed";
                    div.style.left = "-10000px";
                    div.classList.remove("hidden");

                    computedStyle = window.getComputedStyle(div,null);
                    width = computedStyle.width;

                    div.classList.add("hidden");
                    div.style.position = "";
                    div.style.left = "";
                    parent.replaceChild(div,holder);
                    holder = null;

                    div.style.width = "0px";
                    break;
                case HIDING.SCALE_XY:
                    parent = div.parentNode;
                    holder = create(HTML.DIV, {style:{display:"none"}});
                    parent.replaceChild(holder,div);
                    document.body.appendChild(div);
                    div.style.position = "fixed";
                    div.style.left = "-10000px";
                    div.classList.remove("hidden");

                    computedStyle = window.getComputedStyle(div,null);
                    height = computedStyle.height;

                    div.classList.add("hidden");
                    div.style.position = "";
                    div.style.left = "";
                    parent.replaceChild(div,holder);
                    holder = null;

                    width = computedStyle.width;
                    height = computedStyle.height;
                    div.style.width = "0px";
                    div.style.height = "0px";
                    break;
            }

            div.classList.add("hiding-"+animatedType);
            div.classList.add("hiding-animation");

            div.classList.remove("hidden");

            var duration = 200;
            try {
                duration = parseFloat(window.getComputedStyle(div, null).transitionDuration)*1000;
            } catch(e) {
                console.error(e)
            }

            div.hideTask = setTimeout(function(){
                div.classList.remove("hiding-"+animatedType);
                if(height) div.style.height = height;
                if(width) div.style.width = width;
                setTimeout(function(){
                    if(height) div.style.height = "";
                    if(width) div.style.width = "";
                    div.classList.remove("hiding-animation");
                }, duration);
            },0);
        } else {
            div.classList.remove("hidden");
        }
        return div;
    };

    HTMLElement.prototype.hide = function(animatedType) {
        var div = this, computedStyle;
        if(div.classList.contains("hidden")) return;
        clearTimeout(div.hideTask);
        div.isHidden = true;
        if(animatedType) {
            var height,width;
            switch(animatedType) {
                case HIDING.SCALE_Y:
                case HIDING.SCALE_Y_TOP:
                case HIDING.SCALE_Y_BOTTOM:
                    computedStyle = window.getComputedStyle(div,null);
                    height = computedStyle.height;
                    div.style.height = height;
                    break;
                case HIDING.SCALE_X:
                case HIDING.SCALE_X_LEFT:
                case HIDING.SCALE_X_RIGHT:
                    computedStyle = window.getComputedStyle(div,null);
                    width = computedStyle.width;
                    div.style.width = width;
                    break;
                case HIDING.SCALE_XY:
                    computedStyle = window.getComputedStyle(div,null);
                    width = computedStyle.width;
                    height = computedStyle.height;
                    div.style.width = width;
                    div.style.height = height;
                    break;
            }

            div.classList.add("hiding-animation");
            div.classList.add("hiding-"+animatedType);

            computedStyle = window.getComputedStyle(div,null);

            var duration = 200;
            try {
                duration = parseFloat(computedStyle.transitionDuration)*1000;
            } catch(e) {
                console.error(e)
            }

            if(height)div.style.height = "0px";
            if(width)div.style.width = "0px";

            div.hideTask = setTimeout(function(){
                div.classList.add("hidden");
                if(height)div.style.height = "";
                if(width)div.style.width = "";
                div.classList.remove("hiding-"+animatedType);
                div.classList.remove("hiding-animation");
            }, duration);
        } else {
            div.classList.add("hidden");
        }
        return div;
    };

    HTMLElement.prototype.updateHTML = function(update, options) {
        clearTimeout(this._updateTask);
        options = options || {};
        this.innerHTML = update;
        if(!options.noflick) {
            this.classList.add("changed");
            this._updateTask = setTimeout(function(){this.classList.remove("changed")}.bind(this), 5000);
        }
    };

    HTMLElement.prototype.place = function(type, args) {
        if(type && typeof type === "object") {
            args = type;
            type = HTML.DIV;
        } else if(!type) {
            type = HTML.DIV;
        }
        create(type, args, this);
        return this;
    };

    if(!Object.assign) {
        Object.defineProperty(Object.prototype, "assign", {
            enumerable: false,
            value: function(target, first, second) {
                for(var x in first) {
                    if(first.hasOwnProperty(x)) target[x] = first[x];
                }
                for(x in second) {
                    if(second.hasOwnProperty(x)) target[x] = second[x];
                }
                return target;
            }
        });
    }

    if(!String.prototype.toUpperCaseFirst) {
        Object.defineProperty(String.prototype, "toUpperCaseFirst", {
            enumerable: false,
            value: function() {
                return this.substring(0,1).toUpperCase() + this.substring(1);
            }
        });
    }

    if(!String.prototype.sprintf) {
        Object.defineProperty(String.prototype, "sprintf", {
            enumerable: false,
            value: function() {
                var a = this, b;
                if(arguments[0].constructor === Array || arguments[0].constructor === Object) {
                    arguments = arguments[0];
                }
                var args = [];
                for(var i = 0; i < arguments.length; i++) {
                    args.push(arguments[i]);
                }
                return this.replace(/%[\d\.]*[sdf]/g, function(pattern){
                    var value = args.shift();
                    var tokens = pattern.match(/^%(0)?([\d\.]*)(.)$/);
                    switch(tokens[3]) {
                        case "d":
                            var length = +tokens[2];
                            var string = value.toString();
                            if(length > string.length) {
                                tokens[1] = tokens[1] || " ";
                                value = tokens[1].repeat(length - string.length) + string;
                            }
                            break;
                        case "f":
                            break;
                        case "s":
                            break;
                        default:
                            console.error("Unknown pattern: " + tokens[0]);
                    }
                    return value;
                });
            }
        });
    }

    function EPromise() {
        return {
            then: function(callback) {
                this.onResolved = callback || function(){};
                return this;
            },
            catch: function(callback) {
                this.onRejected = callback || function(){};
            },
            onResolved: function(value) {
                console.warn("Define '.then(onResolved(value){...})'; got response: ", value && value.response);
            },
            onRejected: function(code, value, error) {
                console.warn("Define '.catch(onRejected(code, value[, error]){...})'");
                console.error(code, value, error);
            },
            promise: function(newPromise) {
                return newPromise;
            }
        };
    }

    function byId(id) {
        return document.getElementById(id);
    }
    this.byId = byId;

    function normalizeName(name) {
        if(name === HTML.CLASSNAME){
            name = "class";
        } else if(name in attributable) {
        } else if(name.toLowerCase() === "viewbox") {
            name = HTML.VIEWBOX;
        } else if(name !== name.toLowerCase()) {
            var ps = name.split(/([A-Z])/);
            name = ps[0];
            for(var i = 1; i < ps.length; i++) {
                if(i % 2 != 0) name += "-";
                name += ps[i].toLowerCase();
            }
        }
        return name;
    }
    this.normalizeName = normalizeName;

    var attributable = {
        "frameBorder":1,
        "xmlns":1,
        "strokeWidth":1,
        "version":1,
        "fill":1,
        "d":1,
        "tabindex":1,
        "readOnly":1
    };

    function create(name, properties, appendTo, position) {
        var el,namespace,replace = false;
        if(name && typeof name === "object") {
            position = appendTo;
            appendTo = properties;
            properties = name;
            name = HTML.DIV;
        } else if(!name) {
            name = HTML.DIV;
        }

        if(properties && properties.xmlns) {
            el = document.createElementNS(properties.xmlns, name);
            namespace = properties.xmlns;
        } else {
            el = document.createElement(name);
        }

        if(appendTo && typeof appendTo === "string") {
            replace = true;
            properties.id = appendTo;
            appendTo = byId(appendTo);
            if(!properties.innerHTML && appendTo.innerHTML) properties.innerHTML = appendTo.innerHTML;
        }

        if(properties) {
            if(properties instanceof HTMLElement) {
                el.appendChild(properties);
            } else if(properties.constructor === Object) {
                for(var x in properties) {
                    if(!properties.hasOwnProperty(x)) continue;
                    if(x === HTML.INNERHTML || x === HTML.INNERTEXT) {
                        if (properties[x]) {
                            if (properties[x] instanceof HTMLElement) {
                                el.appendChild(properties[x]);
                            } else if (typeof properties[x] === "string") {
                                properties[x] = properties[x].replace(/\$\{(\w+)\}/g, function (x, y) {
                                    return lang[y] ? lang[y].outerHTML : y
                                })
                                el[x] = properties[x];
                            } else {
                                el[x] = properties[x];
                            }
                        }
                    } else if(x == "variable") {
                        create.variables[properties[x]] = el;
                    } else if(x == "childName") {
                        if(appendTo ) {
                            if(appendTo instanceof HTMLElement) {
                                if(appendTo.hasOwnProperty(properties[x])) {
                                    console.warn("Property " + properties[x] + " of node has overrided.");
                                }
                                appendTo[properties[x]] = el;
                            } else {
                                console.error("Property " + properties[x] + " can not be defined for non-HTMLElement.")
                            }
                        } else {
                            console.error("Property " + properties[x] + " can not be defined for null.")
                        }
                    } else if(x == "content" && properties[x].constructor === Array) {
                        for(var i = 0; i < properties[x].length; i++) {
                            el.appendChild(properties[x][i]);
                        }
                    } else if(x === "content" && properties[x].constructor !== String) {
                        el.appendChild(properties[x]);
                    } else if(x === "children" && properties[x]) {
                        var nodes = [];
                        if(properties[x] instanceof HTMLElement) {
                            properties[x].childNodes.forEach(function (item) {
                                nodes.push(item);
                            });
                        } else if (properties[x].constructor === Array) {
                            nodes = properties[x];
                        }
                        nodes.forEach(function(item) {
                            if(item instanceof HTMLElement) {
                                el.appendChild(item);
                            }
                        });
                    } else if(properties[x] instanceof HTMLElement) {
                        el.appendChild(properties[x]);
                        el[x] = properties[x];
                    } else if(x.toLowerCase() === "onlongclick" && properties[x]) {
                        var mousedown,mouseup;
                        el.longclickFunction = properties[x];
                        mousedown = function(evt){
                            clearTimeout(el.longTask);
                            el.addEventListener("mouseup", mouseup);
                            el.addEventListener("touchend", mouseup);
                            el.longTask = setTimeout(function(){
                                el.removeEventListener("mouseup", mouseup);
                                el.removeEventListener("touchend", mouseup);
                                el.longTask = -1;
                                el.longclickFunction(evt);
                            }, 500);
                        };
                        mouseup = function(){
                            clearTimeout(el.longTask);
                        };
                        el.addEventListener("mousedown", mousedown, false);
                        el.addEventListener("touchstart", mousedown, false);
                        el.addEventListener("contextmenu", function(evt){
                            evt.preventDefault();
                            evt.stopPropagation();
                        }, false);
                    } else if(x.toLowerCase() === "onclick") {
                        el.clickFunction = properties[x];
                        if(el.clickFunction) {
                            var call = function(evt) {
                                if(el.longTask && el.longTask < 0) return;
                                el.clickFunction(evt);
                            };
                            el.addEventListener("click", call, false);
                            el.addEventListener("touch", call, false);
                        }
                    } else if(x.indexOf("on") == 0) {
                        var action = x.substr(2).toLowerCase();
                        var call = properties[x];
                        if(call) {
                            el.addEventListener(action, call, false);
                        }
                    } else {
                        var propertyName = normalizeName(x), value = properties[x];
                        if(value != undefined) {
                            if(value.constructor === Object) {
                                var v = "";
                                for(var y in value) {
                                    v += normalizeName(y) + ": " + value[y] + "; ";
                                }
                                value = v;
                            }
                            if(x === "hide" || x === "show") {
                                el[x] = value;
                            } else if(x in el || propertyName in el || propertyName.substr(0,5) === "data-" || x in attributable) {
                                el.setAttribute(propertyName, value);
                            } else {
                                el[x] = value;
                            }
                        }
                    }
                }
            } else if (properties.constructor === String || properties.constructor === Number) {
                el.innerHTML = properties;
            }
        }
        if(appendTo) {
            if(replace) {
                appendTo.parentNode.replaceChild(el, appendTo);
            } else if(appendTo.childNodes.length > 0) {
                if(position === "first") {
                    appendTo.insertBefore(el,appendTo.firstChild);
                } else {
                    appendTo.appendChild(el);
                }
            } else {
                appendTo.appendChild(el);
            }
        }

        return el;
    }
    create.variables = {};
    this.create = create;

    function clear(node) {
        if(!node) return;
        for(var i = node.children.length-1; i>=0; i--) {
            node.removeChild(node.children[i]);
        }
    }
    this.clear = clear;

    function destroy(node) {
        try {
            clear(node);
            if(node.parentNode) node.parentNode.removeChild(node);
            node = null;
        } catch(e) {
            console.error(e);
        }
    }
    this.destroy = destroy;

    function keys(o) {
        var keys = [];
        for(var x in o) {
            keys.push(x);
        }
        return keys;
    }
    this.keys = keys;



    function require(name, context) {
        var origin = name;
        var returned = new EPromise();
        var parts = name.split("/");
        var filename = parts[parts.length-1];
        var onlyname = filename.split(".")[0];
        var needInstantiate = false;
        if(!filename.match(/\.js$/) && parts[1] === "js") {
            needInstantiate = true;
            name += ".js";
        }

        var options = {
            src: name,
            origin:origin,
            module:name,
            instance: needInstantiate ? onlyname : null,
            async: "true",
            defer: "true",
            onload: function(e) {
                var a;
                if(needInstantiate) {
                    if(this.instance && window[this.instance] && window[this.instance].constructor === Function) {
                        try {
                            a = new window[this.instance](context);
                            a.moduleName = this.instance;
                            a.module = this.module;
                            a.origin = this.origin;
                        } catch(e) {
                            returned.onRejected(ERRORS.INVALID_MODULE, this.instance, e);
                        }
                    } else {
                        returned.onRejected(ERRORS.NOT_AN_OBJECT, this.instance, e);
                        return;
                    }
                }
                returned.onResolved(a);
            },
            onerror: function(e) {
                returned.onRejected(ERRORS.NOT_EXISTS, this.instance, e);
            }
        };

        create(HTML.SCRIPT, options, document.head);

        return returned;
    }
    this.require = require;


    function _stringify(key, value) {
        return typeof value === "function" ? value.toString() : value;
    }
    function _parse(key, value) {
        if (typeof value === "string" && /^function.*?\([\s\S]*?\)\s*\{[\s\S]*\}[\s\S]*$/.test(value)) {
            var args = value
                    .replace(/\/\/.*$|\/\*[\s\S]*?\*\//mg, "") //strip comments
                    .match(/\([\s\S]*?\)/m)[0]                      //find argument list
                    .replace(/^\(|\)$/g, "")                    //remove parens
                    .match(/[^\s(),]+/g) || [],                //find arguments
                body = value.replace(/\n/mg, "").match(/\{([\s\S]*)\}/)[1]          //extract body between curlies
            return Function.apply(0, args.concat(body));
        } else {
            return value;
        }
    }
    function save(name, value) {
        if(value) {
            localStorage[self.origin + ":" + name] = JSON.stringify(value, _stringify);
        } else {
            delete localStorage[self.origin + ":" + name];
        }
    }
    this.save = save;

    function load(name) {
        var value = localStorage[self.origin + ":" + name];
        if(value) {
            return JSON.parse(value, _parse);
        } else {
            return null;
        }
    }
    this.load = load;

    function saveForContext(name, value) {
        if(!self.context) {
            save(name, value);
            return;
        }
        if(value) {
            localStorage[self.origin + "$" + self.context +":" + name] = JSON.stringify(value, _stringify);
        } else {
            delete localStorage[self.origin + "$" + self.context +":" + name];
        }
    }
    this.saveForContext = saveForContext;

    function loadForContext(name) {
        if(!self.context) {
            return load(name);
        }
        var value = localStorage[self.origin + "$" + self.context +":"+name];
        if(value) {
            return JSON.parse(value, _parse);
        } else {
            return null;
        }
    }
    this.loadForContext = loadForContext;

    var dialogQueue = [];
    var performingDialogInQueue;

    /**
     * dialog(options [, appendTo])
     * options = {
    *       id,
    *       title: name | {label, className, button},
    *       queue: true|*false*, - if true then post this dialog to the queue and wait
    *       priority: 0-9, - makes sense with queue=true
    *       modal: true|*false*, - if true then dim all behind the dialog and wait for user
    *       hiding: HIDING.method, - default is HIDING.OPACITY
    *       resizeable: true|*false*,
    *       header, - also can set up using setHeader
    *       items, - items can be added also via dialog.addItem
    *       footer, - also can set up using setFooter
    *       positive: button, - also can set up using setPositive
    *       neutral: button, - also can set up using setNeutral
    *       negative: button, - also can set up using setNegative
    *       onopen: function, - also will be called if positive is clicked
    *       onclose: function, - also will be called if negative is clicked
    *       timeout, - dialog will be closed automatically after timeout, onclose will be called
    *       help: function, - question mark will be shown on bottom right corner
    *   }
     * title.button = {
    *       icon,
    *       className,
    *       onclick: function
    *   }
     * button = {
    *       label,
    *       className,
    *       onclick,
    *       dismiss: *true*|false, - if false then dialog will keep shown,
    *   }
     * dialog.addItem(options)
     * options = {
    *       id,
    *       type: HTML.DIV|HTML.A|HTML.SELECT|*HTML.TEXT*|HTML.NUMBER|HTML.TEXTAREA|HTML.BUTTON|HTML.HIDDEN,
    *       className,
    *       labelClassName,
    *       label,
    *       order, - item will be added before another item that has greater order
    *       label|title|innerHTML, - actual for HTML.DIV, can be String or HTMLElement
    *       enclosed: true|*false*, - hide body and show it on click on title
    *       body, - actual only if enclosed:true
    *       value, - actual for HTML.HIDDEN, HTML.SELECT
    *       values, - actual for HTML.SELECT
    *       default, - actual for HTML.SELECT
    *       onclick: function,
    *   }
     * dialog.open()
     * dialog.close()
     * dialog.setHeader(options) - as for create
     * dialog.getHeader()
     * dialog.setFooter(options) - as for create
     * dialog.getFooter()
     * dialog.setPositive(button)
     * dialog.getPositive()
     * dialog.setNeutral(button)
     * dialog.getNeutral()
     * dialog.setNegative(button)
     * dialog.getNegative()
     *
     */
    function dialog(options, appendTo) {
        appendTo = appendTo || document.body;
        options = options || {};

        var dim;
        if(options.modal) {
            dim = create(HTML.DIV, {className:"dialog-dim hidden"}, appendTo);
        }

        var dialog = create(HTML.DIV, {
            className:"modal shadow hidden" + optionalClassName(options.className),
            tabindex:-1,
            onblur: function(evt) {
                if(this._onblur) this._onblur(evt);
                if(this.autoclose) {
                    this.close();
                }
            },
            onfocus: options.onfocus,
            autoclose: options.autoclose,
            _onblur: options.onblur
        }, appendTo);
        dialog.options = options;
        if(dim) {
            dialog.modal = dim;
        }

        dialog.opened = false;

        dialog.clearItems = function() {
            clear(dialog.itemsLayout);
            dialog.itemsLayout.hide();
            dialog.items = [];
        };
        dialog.onaccept = function() {

        };
        dialog.oncancel = function() {

        };

        dialog.addItem = function(item, appendTo) {
            item = item || {};
            appendTo = appendTo || dialog.itemsLayout;
            item.type = item.type || HTML.DIV;

            var div,x;
            if(item.type === HTML.DIV || item.type === HTML.A) {
                if(item.enclosed) {
                    div = x = create(item.type, {
                        className: "dialog-item-enclosed" + optionalClassName(item.className)
                    });
                    var enclosedButton, enclosedIcon;
                    enclosedButton = create(HTML.DIV, {className:"dialog-item-enclosed-button", onclick: function(){
                        if(x.body.classList.contains("hidden")) {
                            enclosedIcon.innerHTML = "expand_less";
                            x.body.show(HIDING.SCALE_Y_TOP);
                            if(item.onopen) {
                                item.onopen(div);
                            }
                        } else {
                            enclosedIcon.innerHTML = "expand_more";
                            x.body.hide(HIDING.SCALE_Y_TOP);
                        }
                    }}, x);
                    enclosedIcon = create(HTML.DIV, {className:"icon dialog-item-enclosed-icon notranslate", innerHTML:"expand_more"}, enclosedButton);
                    create(HTML.DIV, {className:"dialog-item-enclosed-label", innerHTML: item.label || "Show more information"}, enclosedButton);
                    x.body = create(HTML.DIV, {className:"dialog-item-enclosed-body hidden", innerHTML:item.body || ""}, x);
                } else {
                    item.className = "dialog-item" + optionalClassName(item.className);
                    item.innerHTML = item.label || item.title || item.innerHTML || "";
                    delete item.label;
                    delete item.title;
                    var type = item.type;
                    delete item.type;
                    div = x = create(type, item);
                }
            } else if(item.type === HTML.HIDDEN) {
                div = x = create(HTML.INPUT, {type:HTML.HIDDEN, value:item.value || ""});
            } else if(item.type === HTML.SELECT) {
                item.itemClassName = "dialog-item dialog-item-input" + optionalClassName(item.itemClassName);
                div = create(HTML.DIV, {className: item.itemClassName, onclick: function(){this.firstChild.nextSibling.click();}});

                if(item.label) {
                    var labelOptions = {
                        className:"dialog-item-label" + optionalClassName(item.labelClassName)
                    };
                    if(item.label.constructor === String) {
                        labelOptions.innerHTML = item.label;
                    } else {
                        labelOptions.content = item.label;
                    }
                    if(item.id){
                        labelOptions["for"] = item.id;
//                    } else {
//                        create(HTML.DIV, labelOptions , div);
                    }
                    create(HTML.LABEL, labelOptions , div);
                }

                x = create(HTML.SELECT, {
                    type:item.type,
                    className:"dialog-item-input-select" + optionalClassName(item.className),
                    tabindex: i,
                    value:item.value || ""
                }, div);
                for(var y in item.values) {
                    create(HTML.OPTION, {value:y, innerHTML:item.values[y], selected: item.default == y}, x);
                }
            } else {
                item.itemClassName = "dialog-item dialog-item-input" + optionalClassName(item.itemClassName);
                div = create(HTML.DIV, {className:item.itemClassName, onclick: function(){this.lastChild.click();}});

                if(item.label) {
                    var labelOptions = {
                        className:"dialog-item-label" + optionalClassName(item.labelClassName)
                    };
                    if(item.label.constructor === String) {
                        labelOptions.innerHTML = item.label;
                    } else {
                        labelOptions.content = item.label;
                    }
                    if(item.id){
                        labelOptions["for"] = item.id;
                    }
                    create(HTML.LABEL, labelOptions , div);
                }

                var type = HTML.INPUT;
                if(item.type.toLowerCase() === HTML.TEXTAREA) type = HTML.TEXTAREA;
                else if(item.type.toLowerCase() === HTML.BUTTON) type = HTML.BUTTON;

                item.tabindex = i;
                item.className = "dialog-item-input-"+item.type + optionalClassName(item.className);
                if(item.onclick && item.type != HTML.BUTTON) {
                    var a = item.onclick;
                    item.onclick = function(e) { this.focus(); a.call(this); e.stopPropagation(); };
                } else if(item.onclick) {
                } else{
                    item.onclick = function(e) { this.focus(); e.stopPropagation(); };
                }
                item.onkeyup = function(e){
                    if(e.keyCode === 13 && this.type !== HTML.TEXTAREA) {
                        //dialog.close();
                        dialog.onaccept();
                    } else if(e.keyCode === 27) {
                        //dialog.close();
                        dialog.oncancel();
                    }
                };
                item.value = item.value || "";
                delete item.label;
                delete item.labelClassName;

                x = create(type, item, div);
            }
            dialog.items.push(x);

            if(item.order) {
                var appended = false;
                for(var i in appendTo.childNodes) {
                    if(!appendTo.childNodes.hasOwnProperty(i)) continue;
                    if(appendTo.childNodes[i].order > item.order) {
                        appendTo.insertBefore(div, appendTo.childNodes[i]);
                        appended = true;
                        break;
                    }
                }
                if(!appended) appendTo.appendChild(div);

            } else {
                appendTo.appendChild(div);
            }
            dialog.itemsLayout.show();
            return x;
        };
        dialog.addItems = function(items) {
            if(items) {
                items.forEach(function(item){
                    dialog.addItem(item);
                })
            }
            return dialog;
        };
        dialog.setItems = function(items) {
            dialog.clearItems();
            dialog.addItems(items);
            return dialog;
        };

        dialog.adjustPosition = function() {
            var left,top,width,height;
            var id = options.id || (options.title && options.title.label && (options.title.label.dataset && options.title.label.dataset.lang ? options.title.label.dataset.lang : options.title.label));
            if(id) {
                left = load("dialog:"+id+":left");
                top = load("dialog:"+id+":top");
                width = load("dialog:"+id+":width");
                height = load("dialog:"+id+":height");
            }
            if(left || top || width || height) {
                if(left) dialog.style.left = left;
                if(top) dialog.style.top = top;
                if(width) dialog.style.width = width;
                if(height) dialog.style.height = height;
                dialog.style.right = HTML.AUTO;
                dialog.style.bottom = HTML.AUTO;
            } else {
//                left = dialog.offsetLeft;
                var outWidth = appendTo.offsetWidth;

                if((dialog.offsetLeft + dialog.offsetWidth) >= outWidth || left === 0) {
                    dialog.style.left = ((outWidth - dialog.offsetWidth) /2)+"px";
                    dialog.style.top = ((appendTo.offsetHeight - dialog.offsetHeight) /2)+"px";
                    dialog.style.right = "auto";
                    dialog.style.bottom = "auto";
                }
            }
            dialog.focus();
            var focused = false;
            for(var i in dialog.items) {
                if(dialog.items[i].constructor === HTMLInputElement && (dialog.items[i].type === HTML.TEXT || dialog.items[i].type === HTML.NUMBER)) {
                    focused = true;
                    dialog.items[i].focus();
                    break;
                }
            }
            if(!focused) {
                if(dialog.positive && !options.timeout) dialog.positive.focus();
                else if(dialog.negative && options.timeout) dialog.negative.focus();
            }
        };

        var backButtonAction = function(event) {
            window.history.pushState(null, document.title, location.href);
            event.preventDefault();
            event.stopPropagation();
            dialog.close();
        };

        // define the method of animated showing and hiding
        if(options.hiding !== undefined) {
            if(""+options.hiding === "false") {
                options.hiding = "";
            } else if(options.hiding.constructor === String) {
                options.hiding = {
                    open: options.hiding,
                    close: options.hiding
                }
            } else {
                options.hiding.open = options.hiding.open || HIDING.OPACITY;
                options.hiding.close = options.hiding.close || HIDING.OPACITY;
            }
        } else {
            options.hiding = {
                open: HIDING.OPACITY,
                close: HIDING.OPACITY
            }
        }

        dialog.open = function(event){
            var dialog = this;
            if(dialog.opened) return;
            if(dialog.options.queue) {
                if(performingDialogInQueue) {
                    if(dialog.options.priority) {
                        var addedToQueue = false;
                        for(var i in dialogQueue) {
                            if(dialog.options.priority > (dialogQueue[i].options.priority||0)) {
                                dialogQueue.splice(i,0,dialog);
                                addedToQueue = true;
                                break;
                            }
                        }
                        if(!addedToQueue) dialogQueue.push(dialog);
                    } else {
                        dialogQueue.push(dialog);
                    }
                    return;
                } else {
                    performingDialogInQueue = dialog;
                }
            }

            clearInterval(dialog.intervalTask);
            if(dialog.modal) {
                dialog.modal.show();
                dialog.style.zIndex += 10000;
            }
            dialog.show(dialog.options.hiding.open);
            dialog.opened = true;
            dialog.adjustPosition();
            if(dialog.options.onopen) dialog.options.onopen.call(dialog,dialog.items,event);
            if(dialog.offsetHeight) {
                if(dialog.options.timeout) {
                    var atom = dialog.options.timeout / 16;
                    var current = 0;
                    dialog.intervalTask = setInterval(function(){
                        current += 16;
                        dialog.progress.style.width = (current / dialog.options.timeout * 100) + "%";
                        if(current >= dialog.options.timeout) {
                            clearInterval(dialog.intervalTask);
                            dialog.close();
                        }
                    }, 16);
                }
            } else {
                dialog.close();
            }

//            window.history.pushState(null, document.title, location.href);
            if(options.title && options.title.button == defaultCloseButton) {
                window.addEventListener("popstate", backButtonAction);
            }

            return dialog;
        };

        dialog.close = function (event){
            var dialog = this;
            if(!dialog.opened) {
                var index = dialogQueue.indexOf(dialog);
                if(index >= 0) {
                    dialogQueue.splice(index,1);
                }
                return;
            }
            clearInterval(dialog.intervalTask);
            dialog.hide(dialog.options.hiding.close);
            if(dialog.modal) {
                dialog.modal.hide();
                dialog.style.zIndex -= 10000;
            }
            dialog.opened = false;

            window.removeEventListener("popstate", backButtonAction);

            if(dialog.options.onclose) dialog.options.onclose.call(dialog,dialog.items,event);

            if(dialog.options.queue) {
                performingDialogInQueue = null;
                if(dialogQueue.length > 0) {
                    dialog = dialogQueue.shift();
                    dialog.open();
                }
            }

        };
        dialog.addEventListener("keyup", function(e) {
            if(e.keyCode === 27) {
                if(options.negative && options.negative.onclick) {
                    dialog.close();
                    options.negative.onclick.call(dialog,dialog.items);
                }
            }
        });

        options = options || {};

        var defaultCloseButton = {
            icon: " ",
            className: "",
            onclick: function(e){
                dialog.close();
                if(options.negative && options.negative.onclick) options.negative.onclick.call(dialog,dialog.items);
            }
        };
        if(options.title) {
            if(options.title.constructor === String || options.title instanceof HTMLElement) {
                options.title = {
                    label: options.title,
                    className: "",
                    button: defaultCloseButton,
                }
            } else {
                options.title.className = optionalClassName(options.title.className);
                options.title.button = options.title.button || defaultCloseButton;
                if(options.title.button.className) options.title.button.className = " " + options.title.button.className;
                options.title.button.onclick = options.title.button.onclick || function(){};
            }
            var titleLayout = create(HTML.DIV, {
                className:"dialog-title" + optionalClassName(options.title.className),
                onmousedown: function(e) {
                    if(e.button != 0) return;
//                    var position = dialog.getBoundingClientRect();
                    var position = { left: dialog.offsetLeft, top: dialog.offsetTop, width: dialog.offsetWidth, height: dialog.offsetHeight };
                    var offset = [ e.clientX, e.clientY ];
                    var moved = false;
                    function mouseup(e){
                        window.removeEventListener(HTML.MOUSEUP, mouseup, false);
                        window.removeEventListener(HTML.MOUSEMOVE, mousemove, false);
                        var id = options.id || (options.title.label && (options.title.label.dataset.lang ? options.title.label.dataset.lang : options.title.label));
                        if(id && moved) {
                            if(dialog.style.left) save("dialog:"+id+":left", dialog.style.left);
                            if(dialog.style.top) save("dialog:"+id+":top", dialog.style.top);
                        }
                    }
                    function mousemove(e){
                        var deltaX = e.clientX - offset[0];
                        var deltaY = e.clientY - offset[1];
                        if(deltaX || deltaY) {
                            moved = true;
                            dialog.style.left = (position.left + deltaX) + "px";
                            dialog.style.top = (position.top + deltaY ) + "px";
                            dialog.style.right = "auto";
                            dialog.style.bottom = "auto";
                        }
                    }
                    window.addEventListener(HTML.MOUSEUP, mouseup);
                    window.addEventListener(HTML.MOUSEMOVE, mousemove);
                    e.preventDefault();
                },
                ondblclick: function(e) {
                    var id = options.id || (options.title.label && (options.title.label.dataset.lang ? options.title.label.dataset.lang : options.title.label));
                    save("dialog:"+id+":left");
                    save("dialog:"+id+":top");
                    save("dialog:"+id+":width");
                    save("dialog:"+id+":height");
                    dialog.style.left = "";
                    dialog.style.top = "";
                    dialog.style.width = "";
                    dialog.style.height = "";
                    dialog.style.right = "";
                    dialog.style.bottom = "";
                    dialog.adjustPosition();
                }
            }, dialog);
            dialog.titleLayout = create(HTML.DIV, {className:"dialog-title-label", innerHTML: options.title.label }, titleLayout);
            dialog.setTitle = function(title) {
                lang.updateNode(dialog.titleLayout, title);
                //dialog.titleLayout
            };

            if(options.title.button && options.title.button.icon) {
                create(HTML.DIV, {className:"icon dialog-title-button notranslate" + optionalClassName(options.title.button.className), innerHTML:options.title.button.icon, onclick:options.title.button.onclick}, titleLayout);
            }

            if(options.title.filter) {
                dialog.filterLayout = create(HTML.DIV, {
                    className: "dialog-filter"
                }, titleLayout);
                dialog.filterButton = create(HTML.DIV, {
                    className: "icon dialog-filter-button notranslate",
                    innerHTML: "search",
                    onclick: function(evt) {
                        dialog.filterButton.hide();
                        dialog.filterInput.classList.remove("hidden");
                        dialog.filterInput.focus();
                    }
                }, dialog.filterLayout);
                dialog.filterInput = create(HTML.INPUT, {
                    className: "dialog-filter-input hidden",
                    tabindex: -1,
                    onkeyup: function(evt) {
                        if(evt.keyCode === 27) {
                            evt.preventDefault();
                            evt.stopPropagation();
                            if(this.value) {
                                this.value = "";
                            } else {
                                dialog.focus();
                            }
                        }
                        clearTimeout(dialog.filterInput.updateTask);
                        dialog.filterInput.updateTask = setTimeout(function(){
                            dialog.filterInput.apply();
                        }, 300);
                    },
                    onfocus: function(evt) {
                        evt.preventDefault();
                        evt.stopPropagation();
                    },
                    onblur: function() {
                        if(!this.value) {
                            dialog.filterInput.classList.add("hidden");
                            dialog.filterButton.show();
                        }
                    },
                    onclick: function() {
                        this.focus();
                    },
                    apply: function() {
                        if(this.value) {
                            dialog.filterClear.show();
                        } else {
                            dialog.filterClear.hide();
                        }
                        var counter = 0;
                        var substring = this.value.trim().toLowerCase();
                        for(var i in dialog.itemsLayout.childNodes) {
                            if(!dialog.itemsLayout.childNodes.hasOwnProperty(i)) continue;
                            var text = dialog.itemsLayout.childNodes[i].innerText;
                            if(!substring || (text && text.toLowerCase().match(substring))) {
                                dialog.itemsLayout.childNodes[i].show(HIDING.SCALE_Y_TOP);
                                counter++;
                            } else {
                                dialog.itemsLayout.childNodes[i].hide(HIDING.SCALE_Y_TOP);
                            }
                        }
                        if(counter) {
                            dialog.filterPlaceholder.hide();
                            dialog.itemsLayout.show(HIDING.SCALE_Y_TOP);
                        } else {
                            dialog.filterPlaceholder.show();
                            dialog.itemsLayout.hide(HIDING.SCALE_Y_TOP);
                        }
                    }
                }, dialog.filterLayout);
                dialog.filterClear = create(HTML.DIV, {
                    className: "icon dialog-filter-clear hidden notranslate",
                    innerHTML: "clear",
                    onclick: function() {
                        dialog.filterInput.value = "";
                        dialog.filterInput.focus();
                        dialog.filterInput.apply();
                    }
                }, dialog.filterLayout);
            }

        }

        dialog.header = create(HTML.DIV, {className:"hidden"}, dialog);
        dialog.getHeader = function() { return dialog.header };
        dialog.setHeader = function(item) {
            if(!item) {
                dialog.header.hide();
                dialog.header.innerHTML = "";
                return dialog.header;
            }
            item.className = "dialog-header" + optionalClassName(item.className);
            item.innerHTML = item.label || item.title || item.innerHTML || "";
            delete item.label;
            delete item.title;
            var type = item.type;
            delete item.type;
            var node = create(type, item);
            dialog.header.parentNode.replaceChild(node, dialog.header);
            dialog.header = node;
            return dialog.header;
        };
        if(options.header) {
            dialog.setHeader(options.header);
        }

        dialog.itemsLayout = create(HTML.DIV, {className:"dialog-items" +optionalClassName(options.itemsClassName)}, dialog);
        dialog.items = [];
        if(options.items) {
            dialog.addItems(options.items);
        }

        if(options.title && options.title.filter) {
            dialog.filterPlaceholder = create(HTML.DIV, {className: "dialog-items hidden",innerHTML: "Nothing found"}, dialog);
        }

        dialog.footer = create(HTML.DIV, {className:"hidden"}, dialog);
        dialog.getFooter = function() { return dialog.footer };
        dialog.setFooter = function(item) {
            if(!item) {
                dialog.footer.hide();
                dialog.footer.innerHTML = "";
                return dialog.footer;
            }
            item.className = "dialog-footer" + optionalClassName(item.className);
            item.innerHTML = item.label || item.title || item.innerHTML || "";
            delete item.label;
            delete item.title;
            var type = item.type;
            delete item.type;
            var node = create(type, item, dialog);
            dialog.footer.parentNode.replaceChild(node, dialog.footer);
            dialog.footer = node;
            return dialog.footer;
        };
        if(options.footer) {
            dialog.setFooter(options.footer);
        }

        var buttons = create(HTML.DIV, {className:"dialog-buttons hidden" + optionalClassName(options.buttonsClassName)}, dialog);
        dialog.positive = create(HTML.BUTTON, {className: "hidden"}, buttons);
        dialog.neutral = create(HTML.BUTTON, {className: "hidden"}, buttons);
        dialog.negative = create(HTML.BUTTON, {className: "hidden"}, buttons);

        dialog.getPositive = function() {
            return dialog.positive;
        };
        dialog.setPositive = function(item) {
            if(!item) {
                dialog.positive.hide();
                dialog.onaccept = function() {
                };
                return dialog.positive;
            }
            item.tabindex = 98;
            item.className = "dialog-button dialog-button-positive" + optionalClassName(item.className);
            item._onclick = item.onclick;
            item.onclick = function(event){
                if(item._onclick) item._onclick.call(dialog,dialog.items,event);
                if(item.dismiss == undefined || item.dismiss) dialog.close();
            };
            item.innerHTML = item.label;

            if(dialog.positive) {
                var node = create(HTML.BUTTON, item, buttons);
                dialog.positive.parentNode.replaceChild(node, dialog.positive);
                dialog.positive = node;
            } else {
                dialog.positive = create(HTML.BUTTON, item, buttons);
            }
            buttons.show();
            dialog.onaccept = function() {
                item.onclick.call(dialog,dialog.items);
            };
            return dialog.positive;
        };
        if(options.positive && options.positive.label) {
            dialog.setPositive(options.positive);
        }

        dialog.getNeutral = function() {
            return dialog.neutral;
        };
        dialog.setNeutral = function(item) {
            if(!item) {
                dialog.neutral.hide();
                dialog.oncancel = function() {
                };
                return dialog.neutral;
            }
            item.tabindex = 100;
            item.className = "dialog-button dialog-button-neutral" + optionalClassName(item.className);
            item._onclick = item.onclick;
            item.onclick = function(event){
                if(item._onclick) item._onclick.call(dialog,dialog.items,event);
                if(item.dismiss == undefined || item.dismiss) dialog.close();
            };
            item.innerHTML = item.label;

            if(dialog.neutral) {
                var node = create(HTML.BUTTON, item, buttons);
                dialog.neutral.parentNode.replaceChild(node, dialog.neutral);
                dialog.neutral = node;
            } else {
                dialog.neutral = create(HTML.BUTTON, item, buttons);
            }
            dialog.oncancel = function() {
                item.onclick.call(dialog,dialog.items);
            };
            buttons.show();
            return dialog.neutral;
        };
        if(options.neutral && options.neutral.label) {
            dialog.setNeutral(options.neutral);
        }

        dialog.getNegative = function() {
            return dialog.negative;
        };
        dialog.setNegative = function(item) {
            if(!item) {
                dialog.negative.hide();
                return dialog.negative;
            }
            item.tabindex = 99;
            item.className = "dialog-button dialog-button-negative" + optionalClassName(item.className);
            item._onclick = item.onclick;
            item.onclick = function(event){
                if(item._onclick) item._onclick.call(dialog,dialog.items,event);
                if(item.dismiss == undefined || item.dismiss) dialog.close();
            };
            item.innerHTML = item.label;

            if(dialog.negative) {
                var node = create(HTML.BUTTON, item, buttons);
                dialog.negative.parentNode.replaceChild(node, dialog.negative);
                dialog.negative = node;
            } else {
                dialog.negative = create(HTML.BUTTON, item, buttons);
            }
            buttons.show();
            return dialog.negative;
        };
        if(options.negative && options.negative.label) {
            dialog.setNegative(options.negative);
        }

        if(options.help) {
            create(HTML.BUTTON, {className:"dialog-help-button", onclick:options.help, innerHTML:"help_outline"}, dialog);
        }
        if(options.resizeable) {
            create(HTML.DIV, {
                className:"dialog-resize",
                onmousedown: function(e) {
                    if(e.button != 0) return;
//                    var position = dialog.getBoundingClientRect();
                    var position = { left: dialog.offsetLeft, top: dialog.offsetTop, width: dialog.offsetWidth, height: dialog.offsetHeight };
                    var offset = [ e.clientX, e.clientY ];
                    var moved = false;
                    function mouseup(e){
                        window.removeEventListener(HTML.MOUSEUP, mouseup, false);
                        window.removeEventListener(HTML.MOUSEMOVE, mousemove, false);
                        if((options.id || options.title.label) && moved) {
                            var id = options.id || (options.title.label && (options.title.label.dataset.lang ? options.title.label.dataset.lang : options.title.label));
                            if(dialog.style.width) save("dialog:"+id+":width", dialog.style.width);
                            if(dialog.style.height) save("dialog:"+id+":height", dialog.style.height);
                        }
                    }
                    function mousemove(e){
                        var deltaX = e.clientX - offset[0];
                        var deltaY = e.clientY - offset[1];
                        if(deltaX || deltaY) {
                            moved = true;
                            dialog.style.width = (position.width + deltaX)+"px";
                            dialog.style.height = (position.height + deltaY)+"px";
                        }
                    }
                    window.addEventListener(HTML.MOUSEUP, mouseup);
                    window.addEventListener(HTML.MOUSEMOVE, mousemove);
                    e.preventDefault();
                }
            }, dialog);
        }

        if(options.timeout) {
            var progressBar = create(HTML.DIV, {className:"dialog-progress-bar"}, dialog);
            dialog.progress = create(HTML.DIV, {className:"dialog-progress-value"}, progressBar);
            dialog.progress.style.width = "0%";
        }

        return dialog;
    }
    this.dialog = dialog;

    /*
        function sprintf() {
            var a = this, b;
            if(arguments[0].constructor === Array || arguments[0].constructor === Object) {
                arguments = arguments[0];
            }
            var args = [];
            for(var i = 0; i < arguments.length; i++) {
                args.push(arguments[i]);
            }
            return this.replace(/%[\d\.]*[sdf]/g, function(pattern){
                var value = args.shift();
                var tokens = pattern.match(/^%(0)?([\d\.]*)(.)$/);
                switch(tokens[3]) {
                case "d":
                    if(tokens[1] == "0") {
                        var length = +tokens[2];
                        var string = value.toString();
                        value = "0".repeat(length - string.length) + string;
                    }
                    break;
                case "f":
                    break;
                case "s":
                    break;
                default:
                    console.error("Unknown pattern: " + tokens[0]);
                }
                return value;
            });
        }
        this.sprintf = sprintf;
    */

    function cloneAsObject(object) {
        var o = {};
        for(var x in object) {
            if(!object[x] || object[x].constructor === String || object[x].constructor === Number) {
                o[x] = object[x] || "";
            } else {
                o[x] = cloneAsObject(object[x]);
            }
        }
        return o;
    }
    this.cloneAsObject = cloneAsObject;

    function lang(string, value) {
        if(value) {
            var prev = lang.$origin[string];
            lang.$origin[string] = value;
            if(!prev) {
                Object.defineProperty(lang, string, {
                    get: function() {
                        lang.$nodes[string] = lang.$nodes[string] || create(HTML.SPAN, {
                            dataLang: string
                        });
                        var a = lang.$nodes[string].cloneNode();
                        a.format = function() {
                            lang.$arguments[this.dataset.lang] = arguments;
                            this.innerHTML = lang.$origin[this.dataset.lang] || (this.dataset.lang ? this.dataset.lang.substr(0,1).toUpperCase() + this.dataset.lang.substr(1) : "");
                            this.innerHTML = this.innerHTML.sprintf(arguments);
                            return this;
                        };
                        a.innerHTML = lang.$origin[string] || (string ? string.substr(0,1).toUpperCase() + string.substr(1) : "");
                        if(lang.$arguments[string]){
                            a.innerHTML = a.innerHTML.sprintf(lang.$arguments[string]);
                        }
                        a.dataset.lang = string;
                        return a;
                    }
                });
            }
        }
        return (lang.$origin[string] && lang[string]) || (string ? string.substr(0, 1).toUpperCase() + string.substr(1) : "");
    }
    this.lang = lang;

    lang.$nodes = lang.$nodes || {};
    lang.$origin = lang.$origin || {};
    lang.$arguments = lang.$arguments || {};

    lang.overrideResources = function(options) {
        if(options.locale == "en") {
            lang._overrideResources(options);
        } else {
            lang._overrideResources({
                "default": options.default,
                resources: options.default,
                type: options.type,
                resource: options.resource,
                locale: "en",
                callback: function() {
                    lang._overrideResources(options);
                }
            });
        }
    };

    lang._overrideResources = function(options) {
        if(!options || !options.default) {
            console.error("Not defined default resources");
            return;
        }

        options.resources = options.resources || options.default;

        if(options.resources.constructor === String) {
            getJSON(options.resources, options).then(function(json){
                var nodes = document.getElementsByTagName(HTML.SPAN);
                console.warn("Switching to resources \""+(options.locale || options.resources)+"\".");
                for(var x in json) {
//                            if(lang.$origin[x]) {
//                                console.warn("Overrided resources: " + x + ":", json[x] ? (json[x].length > 30 ? json[x].substr(0,30)+"..." : json[x]) : "" );
//                            }
                    lang(x, json[x]);
                }
                for(var i = 0; i < nodes.length; i++) {
                    if(nodes[i].dataset.lang) {
                        try {
                            nodes[i].parentNode.replaceChild(lang[nodes[i].dataset.lang],nodes[i]);
                        } catch(e) {
                            console.warn("Resource not found: " + nodes[i].dataset.lang);
                        }
                    }
                }
                if(options.callback) options.callback();
            }).catch(function(code, xhr, error){
                switch(code) {
                    case ERRORS.ERROR_LOADING:
                        console.warn("Error fetching resources for",options,xhr.status + ': ' + xhr.statusText);
                        if(options.default != options.resources){
                            console.warn("Switching to default resources \""+options.default+"\".");
                            lang._overrideResources({"default":options.default});
                        }
                        break;
                    case ERRORS.INCORRECT_JSON:
                        console.warn("Incorrect, empty or damaged resources file for",options,error,xhr);
                        if(options.default != options.resources){
                            console.warn("Switching to default resources \""+options.default+"\".");
                            lang._overrideResources({"default":options.default});
                        }
                        break;
                    default:
                        console.warn("Incorrect, empty or damaged resources file for",options,error,xhr);
                        break;
                }
                if(options.default != options.resources){
                    console.warn("Switching to default resources \""+options.default+"\".");
                    lang._overrideResources({"default":options.default});
                } else {
                    if(options.callback) options.callback();
                }
            });

        } else if(options.resources.resources) {
            for(var x in options.resources.resources) {
                if(lang[x]) {
//                    console.warn("Overrided resources: " + x + ":", holder.resources[x] ? (holder.resources[x].length > 30 ? holder.resources[x].substr(0,30)+"..." : holder.resources[x]) : "" );
                }
                lang(x, options.resources.resources[x]);
            }
        }
    };

    lang.updateNode = function(node, lang) {
        if(typeof lang === "string") {
            node.innerHTML = lang;
        } else if(node && lang && lang.dataset && lang.dataset.lang) {
            node.innerHTML = lang.outerHTML;
        }
    };

    /**
     get(url [, post])
     .then(callback(xhr))
     .catch(callback(code,xhr));
     */

    function rest(method, url, body) {
        var returned = new EPromise();
        var xhr = new XMLHttpRequest();

        xhr.open(method, url, true);
//        xhr.setRequestHeader("Access-Control-Allow-Origin", "*");
        xhr.onreadystatechange = function() { // (3)
            if (xhr.readyState != 4) return;
            if (xhr.status != 200) {
                returned.onRejected(xhr.status, xhr);
            } else {
                returned.onResolved(xhr);
            }
        };
        try {
            if(body) {
                if(body.constructor === Object) {
                    body = JSON.stringify(body);
                }
                xhr.send(body);
            } else {
                xhr.send();
            }
        } catch(e) {
            returned.onRejected(ERRORS.ERROR_SENDING_REQUEST, xhr);
            return;
        }
        return returned;
    }

    function get(url) {
        return rest("GET",url);
    }
    this.get = get;

    function post(url, body) {
        return rest("POST", url, body);
    }
    this.post = post;

    function put(url) {
        return rest("PUT",url);
    }
    this.put = put;

    /**
     getJSON(url [, post])
     .then(callback(xhr))
     .catch(callback(code,xhr));
     */
    function getJSON(url, body) {
        var callbacks = {
            then: function(json,xhr) { console.warn("Define .then(callback(json,xhr){...})")},
            "catch": function(code, xhr) { console.error(code, xhr); }
        };
        var catchFunction = function(callback) {
            callbacks.catch = callback;
        };
        var thenFunction = function(callback) {
            callbacks.then = function(xhr) {
                try {
                    var text = xhr.responseText;
                    text = text.replace(/\/\*[\s\S]*?\*\//g, "").replace(/[\r\n]+/gm, " ");
                    var json = JSON.parse(text);
                    callback(json, xhr);
                } catch(e) {
                    callbacks.catch(ERRORS.INCORRECT_JSON, xhr, e);
                }
            };
            return { "catch": catchFunction };
        };
        setTimeout(function(){
            if(body) post(url, body).then(callbacks.then).catch(callbacks.catch);
            else get(url).then(callbacks.then).catch(callbacks.catch);
        },0);
        return { then: thenFunction, "catch": catchFunction };
    }
    this.getJSON = getJSON;

    function drawer(options, appendTo) {
//        collapsed = options.collapsed;
        var collapsed = load(options.collapsed);
        if(options.collapsed == undefined) {
            collapsed = load("drawer:collapsed");
            options.collapsed = "drawer:collapsed";
        } else if(typeof options.collapsed === "boolean") {
            collapsed = options.collapsed;
            options.collapsed = "drawer:collapsed";
        } else {
            collapsed = load(options.collapsed);
        }

        var footerButtonCollapseDiv;
        var footerButtonExpandDiv;

        var footerButtonSvg = {
            xmlns:"http://www.w3.org/2000/svg",
            viewbox:"2 2 14 14",
            fit: "",
            version:"1.1",
            width: 24,
            height: 24,
            preserveAspectRatio: "xMidYMid meet",
            className: "icon drawer-menu-item-icon drawer-footer-button",
            onclick: function(e) {
                save(options.collapsed, !collapsed);
                this.replaceChild(collapsed ? footerButtonExpandDiv : footerButtonCollapseDiv, this.firstChild);
            }
        };
        var footerButtonCollapsePath = {
            xmlns:"http://www.w3.org/2000/svg",
            d: "M5.46 8.846l3.444-3.442-1.058-1.058-4.5 4.5 4.5 4.5 1.058-1.057L5.46 8.84zm7.194 4.5v-9h-1.5v9h1.5z",
        };
        var footerButtonExpandPath = {
            xmlns:"http://www.w3.org/2000/svg",
            d: "M5.46 8.846l3.444-3.442-1.058-1.058-4.5 4.5 4.5 4.5 1.058-1.057L5.46 8.84zm7.194 4.5v-9h-1.5v9h1.5z"
        };

        var swipeHolder = function(e){

            var touch;
            if(e.changedTouches) touch = e.changedTouches[0];

            var startX = e.pageX || touch.pageX;
            var lastX = startX;
            var lastDelta = 0;

            var endHolder = function(e){
                window.removeEventListener("touchend",endHolder);
                window.removeEventListener("touchmove",moveHolder);
                layout.style.transition = "";

                if(e.changedTouches) touch = e.changedTouches[0];
                var x = parseInt(layout.style.left || 0)
                if(lastDelta < -20 || (lastDelta <=0 && x < -layout.offsetWidth/2)) {
                    layout.style.left = (-layout.offsetWidth*1.5)+"px";
                    setTimeout(function(){layout.close()},500);
                } else {
                    layout.style.left = "";
                }
            };
            var moveHolder = function(e) {
                var delta;
                if(e.changedTouches) touch = e.changedTouches[0];
                delta = (e.pageX || touch.pageX) - startX;
                lastDelta = (e.pageX || touch.pageX) - lastX;
                lastX = e.pageX || touch.pageX;

                if(delta <= -10) {
                    layout.style.left = delta + "px";
//                    e.preventDefault();
                    e.stopPropagation();
                }
            };
            window.addEventListener("touchend", endHolder);
            window.addEventListener("touchmove", moveHolder);

            layout.style.transition = "none";
        };


        var layout = create(HTML.DIV, {
            className:"drawer changeable" + (collapsed ? " drawer-collapsed" : "") + optionalClassName(options.className),
            tabindex: -1,
            onblur: function(){
                layout.close();
                return true;
            },
            open: function() {
                this.classList.add("drawer-open");
                this.style.left = "";
                this.scrollTop = 0;
                this.menu.scrollTop = 0;
                this.focus();
            },
            close: function(){
                this.classList.remove("drawer-open");
            },
            toggle: function() {
                if(this.classList.contains("drawer-open")) {
                    this.blur();
                } else {
                    this.open();
                }
            },
            items: {},
            sections: [],
            toggleSize: function(force) {
                collapsed = !collapsed;
                if(force != undefined) collapsed = force;
                save("drawer:collapsed", collapsed);
                layout.toggleButton.innerHTML = collapsed ? "last_page" : "first_page";
                layout.classList[collapsed ? "add" : "remove"]("drawer-collapsed");
                layoutHeaderHolder.classList[collapsed ? "add" : "remove"]("drawer-collapsed");
                if(options.ontogglesize) options.ontogglesize(force);
            },
            ontouchstart: swipeHolder
//            onmousedown: swipeHolder
        });
        if(typeof appendTo === "string") {
            appendTo = byId(appendTo);
            appendTo.parentNode.replaceChild(layout, appendTo);
        } else {
            appendTo.insertBefore(layout, appendTo.firstChild);
        }

        layout.frame = create("iframe", {width:"100%",height:"1%", className:"drawer-iframe"}, layout);
        layout.frame.contentWindow.addEventListener("resize",function(){
            if(!layout.resizeTask) layout.resizeTask = setTimeout(function(){
                if(options.ontogglesize) options.ontogglesize();
                delete layout.resizeTask;
            }, 500);
        });

        var layoutHeaderHolder = create(HTML.DIV, {className: "drawer-header-holder changeable"});
        layout.parentNode.insertBefore(layoutHeaderHolder, layout);
        layout.header = create(HTML.DIV, { className:"drawer-header changeable" }, layout);

        if(options.logo) {
            create(HTML.IMG, {
                className:"drawer-header-logo changeable",
                src:options.logo.src,
                onclick: options.logo.onclick
            }, layout.header);
        }
        layout.headerPrimary = create(HTML.DIV, {className:"drawer-header-name changeable", onclick: function(evt){
            layout.blur();
            if(options.onprimaryclick) options.onprimaryclick();
        }}, layout.header);
        layout.headerTitle = create(HTML.DIV, {className:"drawer-header-title changeable", innerHTML:options.title}, layout.header);
        create(HTML.DIV, {className:"drawer-header-subtitle changeable", innerHTML: options.subtitle }, layout.header);


        layout.menu = create(HTML.DIV, {className:"drawer-menu changeable"}, layout);
        options.sections = options.sections || {};
        for(var i=0;i<10;i++){
            layout.sections[i] = create({order:i, className:"hidden" + (i===9 ? "" : " drawer-menu-divider")}, layout.menu)
                .place({className: "drawer-menu-section-title media-hidden"})
                .place({});
            if(options.sections[i]) {
                layout.sections[i].firstChild.place({className: "drawer-menu-section-label", innerHTML: options.sections[i]});
            }

            if(options.collapsible && options.collapsible.indexOf(i) >= 0) {
                var sectionCollapsed = load("drawer:section:collapsed:"+i);
                if(sectionCollapsed) layout.sections[i].lastChild.hide();

                layout.sections[i].firstChild.addEventListener("click", function(){
                    if(this.nextSibling.isHidden) {
                        this.nextSibling.show(HIDING.SCALE_Y_TOP);
                        this.lastChild.show();
                        this.lastChild.previousSibling.hide();
                        save("drawer:section:collapsed:"+this.parentNode.order);
                    } else {
                        this.nextSibling.hide(HIDING.SCALE_Y_TOP);
                        this.lastChild.hide();
                        this.lastChild.previousSibling.show();
                        save("drawer:section:collapsed:"+this.parentNode.order, true);
                    }
                });
                layout.sections[i].firstChild.place({ className: "icon drawer-menu-item drawer-menu-item-expand notranslate" + (sectionCollapsed ? "" : " hidden"), innerHTML: "expand_more"});
                layout.sections[i].firstChild.place({ className: "icon drawer-menu-item drawer-menu-item-collapse notranslate" + (sectionCollapsed ? " hidden" : ""), innerHTML: "expand_less"});
            }
        }

        layout.add = function(section,id,name,icon,callback) {
            layout.items[id] = {
                name:name,
                icon:icon,
                callback:callback
            };
            var th = create(HTML.DIV, {
                className:"drawer-menu-item",
                onclick: function (event) {
                    var self = this;
                    setTimeout(function () {
                        callback.call(self,event);
                        layout.blur();
                    }, 100);
                },
                hide: function() {
                    this.classList.add("hidden");
                    this.fixShowing();
                    return this;
                },
                show: function() {
                    this.classList.remove("hidden");
                    this.fixShowing();
                    return this;
                },
                enable: function() {
                    this.classList.remove("disabled");
                    return this;
                },
                disable: function() {
                    this.classList.add("disabled");
                    return this;
                },
                fixShowing: function() {
                    var parent = th.parentNode.parentNode;
                    var shown = false;
                    for(var i in parent.childNodes) {
                        if(parent.childNodes.hasOwnProperty(i)) {
                            if(!parent.childNodes[i].classList.contains("hidden")) shown = true;
                        }
                    }
                    if(shown) parent.show();
                    else parent.hide();
                },
                increaseBadge: function() {
                    var val = parseInt(this.badge.innerHTML || "0");
                    val ++;
                    this.badge.innerHTML = val;
                    this.showBadge();
                },
                showBadge: function() {
                    this.badge.show();
                },
                hideBadge: function() {
                    this.badge.hide();
                    this.badge.innerHTML = "0";
                }
            }, layout.sections[section].lastChild);

            if(icon) {
                if(icon.constructor === String) {
                    create(HTML.DIV, { className:"icon drawer-menu-item-icon notranslate", innerHTML: icon }, th);
                } else {
                    th.appendChild(icon);
                }
            }
            if(callback) {
                create(HTML.DIV, {
                    className: "drawer-menu-item-label",
                    innerHTML: name
                }, th);
            }
            th.badge = create(HTML.DIV, { className:"drawer-menu-item-badge hidden", innerHTML: "0" }, th);
            layout.sections[section].show();

            return th;
        };

        layout.footer = create(HTML.DIV, { className:"drawer-footer"}, layout);

        footerButtonCollapseDiv = create(HTML.PATH, footerButtonCollapsePath);
        footerButtonExpandDiv = create(HTML.PATH, footerButtonExpandPath);

        layout.toggleButton = create(HTML.DIV, {className: "icon drawer-menu-item-icon drawer-footer-button notranslate", innerHTML: collapsed ? "last_page" : "first_page", onclick: function(e){
            layout.toggleSize();
        }}, layout.footer);
        if(options.footer) {
            create(HTML.DIV, options.footer, layout.footer);
        }

        return layout;
    }
    this.drawer = drawer;

    function toast() {
        var toast = create(HTML.DIV, {className:"toast-holder hidden", onclick: function(){ this.hide(HIDING.SCALE_Y_BOTTOM); }});
        toast.content = create(HTML.DIV, {className:"toast shadow"}, toast);
        toast.show = function(text,delay){
            if(!toast.parentNode) document.body.appendChild(toast);
            clearTimeout(toast.hideTask);
            lang.updateNode(toast.content, text);
            HTMLDivElement.prototype.show.call(toast, HIDING.SCALE_Y_BOTTOM);
            delay = delay || 5000;
            if(delay > 0) {
                toast.hideTask = setTimeout(function(){
                    toast.hide(HIDING.SCALE_Y_BOTTOM);
                },delay);
            }
        };
        return toast;
    }
    this.toast = new toast();

    function notification(options) {
        if(!options.persistent && !document.hidden) return;
        if(load("main:disable_notification")) return;
        if (!("Notification" in window)) {
            console.error("This browser does not support desktop notification");
        } else {
            Notification.requestPermission(function(result) {
                if(result === "granted") {
                    var title = options.title;
                    delete options.title;
                    var notif;
                    try {
                        notif = new Notification(title, options);
                    } catch (e) {
                        if(e.name === "TypeError") {
                            navigator.serviceWorker.register("/sw.js").then(function(e){
                                navigator.serviceWorker.ready.then(function(registration) {
                                    notif = registration.showNotification(title, options);
                                });
                            });
                        }
                    }
                    notif.onclick = function(e){
                        notif.close();
                        window.focus();
                        if(options.onclick) options.onclick(e);
                        else {console.warn("Redefine onclick.")}
                    };
                    if(options.duration) {
                        setTimeout(function(){
                            notif.close();
                        }, options.duration);
                    }
                }
            });
        }
    }
    this.notification = notification;

    function actionBar(options, appendTo) {

        var actionbar = create(HTML.DIV, {
            className:"actionbar changeable" + optionalClassName(options.className),
            toggleSize: function(force){
                var cvollapsed = actionbar.classList.contains("actionbar-collapsed");
                if(force != undefined) collapsed = force;
                actionbar.classList[collapsed ? "add" : "remove"]("actionbar-collapsed");
                actionbarHolder.classList[collapsed ? "add" : "remove"]("actionbar-collapsed");
                if(options.ontogglesize) options.ontogglesize(force);
            },
            setTitle: function(text) {
                if(text instanceof HTMLElement) {
                    actionbar.titleNode.innerHTML = text.outerHTML;
                } else {
                    actionbar.titleNode.innerHTML = text;
                }
            }
        });
        create(HTML.SPAN, {innerHTML:"menu", className:"actionbar-button", onclick: options.onbuttonclick, onfocus:function(){}}, actionbar);
        var label = create(HTML.DIV, {className:"actionbar-label changeable"}, actionbar);
        actionbar.titleNode = create(HTML.DIV, {className:"actionbar-label-title changeable", innerHTML: options.title || ""}, label);
        actionbar.subtitle = create(HTML.DIV, {className:"actionbar-label-subtitle changeable", innerHTML: options.subtitle || ""}, label);

        if(typeof appendTo === "string") {
            appendTo = byId(appendTo);
            appendTo.parentNode.replaceChild(actionbar, appendTo);
        } else {
            appendTo.insertBefore(actionbar, appendTo.firstChild);
        }

        var actionbarHolder = create(HTML.DIV, {className: "actionbar-holder changeable"});
        actionbar.parentNode.insertBefore(actionbarHolder, actionbar);


        return actionbar;
    }
    this.actionBar = actionBar;

    function copyToClipboard(input) {
        if(!input) return false;
        input.focus();
        input.select();

        try {
            return document.execCommand('copy');
        } catch(err) {
            return false;
        }
    }
    this.copyToClipboard = copyToClipboard;

    function table(options, appendTo) {
        options.className = "table" + optionalClassName(options.className);
        var table = create(HTML.DIV, {
            className: options.className,
            filter: function() {
                if(!options.caption.items) return;
                setTimeout(function(){
                    for(var i in table.rows) {
                        var valid = true;
                        for(var j in table.filter.options) {
                            if(table.filter.options[j]) {
                                valid = table.filter.options[j].call(table,table.rows[i]);
                            }
                            if(!valid) break;
                        }
                        var row = table.rows[i];
                        if(valid && row.isHidden) {
                            row.show();
                        } else if (!valid && !row.isHidden) {
                            row.hide();
                        }
                    }
                },0);
            },
            rows: [],
            saveOption: function(name,value) {
                if(options.id) {
                    delete savedOptions[name];
                    savedOptions[name] = value;
                    save("table:" + options.id, savedOptions);
                }
            },
            add: function(row) {
                row = row || {};
                row.className = "tr" +(row.onclick ? " clickable":"")+optionalClassName(row.className);

                var res = create(HTML.DIV, row, table.body);
                res.cells = [];

//                 var res = create(HTML.DIV, {className:"tr"+(row.onclick ? " clickable":"")+(row.className ? " "+row.className : ""), onclick: row.onclick, cells: [] }, table.body);
                for(var i in row.cells) {
                    var item = row.cells[i];
                    item.className = "td" + optionalClassName(item.className);
                    item.innerHTML = item.innerHTML || item.label;
                    res.cells.push(create(HTML.DIV, item, res));
                }
                table.rows.push(res);
                table.placeholder.hide();
                table.update();
                return res;
            },
            update: function() {
                if(!options.caption.items) return;

                clearTimeout(table.updateTask);
                table.updateTask = setTimeout(function(){
                    table.filter();
                    for(var i in table._sorts) {
                        try{
                            var index = table._sorts[i].index;
                            table.head.cells[index].sort = table._sorts[i].mode;
                            table.sort(index);
                        } catch(e) {
                            console.error(e);
                        }
                    }
                }, 0);

            },
            sort: function(index) {
                if(!options.caption.items) return;

                var sort = table.head.cells[index].sort;

                table.head.cells[index].firstChild.show();
                table.head.cells[index].firstChild.classList[sort > 0 ? "add" : "remove"]("table-sort-descend");

                var rows = [];
                for(var i = 0; i < table.body.childNodes.length; i++) {
                    rows.push(table.body.childNodes[i]);
                }

                rows.sort(function(a, b) {
                    var aCriteria = a.cells[index].sort == undefined ? a.cells[index].innerText.toLowerCase() : a.cells[index].sort;
                    var bCriteria = b.cells[index].sort == undefined ? b.cells[index].innerText.toLowerCase() : b.cells[index].sort;

                    return aCriteria == bCriteria ? 0 : (aCriteria > bCriteria ? 1 : -1) * sort;
                });


                for(i in rows) {
                    table.body.appendChild(rows[i]);
                }

            },
            _sorts: [],
            sorts: function(options) {
                if(!options) return table._sorts;
                for(var i in table._sorts) {
                    if(table._sorts[i].index == options.index) {
                        table._sorts.splice(i,1);
                        break;
                    }
                }
                if(options.mode) table._sorts.push(options);
                table.saveOption("sorts",table._sorts);
            }
        });

        if(appendTo) appendTo.appendChild(table);

        options.caption = options.caption || {};
        options.caption.className = "thead" + optionalClassName(options.caption.className);
        if(options.caption.items) {
            table.head = create(HTML.DIV, {className:options.caption.className}, table);
            table.head.cells = [];
//            var div = create(HTML.DIV, {className:"tr"}, table.head);
            for(var i in options.caption.items) {
                var item = options.caption.items[i];
                item.className = "th"+optionalClassName(item.className);
                var innerHTML = item.innerHTML;
                delete item.innerHTML;
                if(options.sort == undefined || options.sort) {
                    item.index = i;
                    item.sort = 0;
                    item.onclick = function() {
                        this.sort ++;
                        if(this.sort == 0) this.sort ++;
                        else if(this.sort > 1) this.sort = -1;

                        table.sorts({ index: this.index, mode: this.sort });
                        table.sort(this.index);

                    };
                    item.ondblclick = function() {
                        this.sort = 0;
                        table.sorts({ index: this.index });
                        table.head.cells[this.index].firstChild.hide();
                        table.update();
                    };
                }
                var cell = create(HTML.DIV, item, table.head);
                cell.place(HTML.DIV,{className:"table-sort hidden", innerHTML:"sort"}).place(HTML.SPAN, {innerHTML: item.innerHTML || item.label});
                table.head.cells.push(cell);
            }

            if((options.filter == undefined || options.filter) || (options.sort == undefined || options.sort)) {
                table.resetButton = create(HTML.DIV, {
                    className: "table-reset-button notranslate",
                    innerHTML: "clear_all",
                    title: "Reset customizations",
                    onclick: function() {
                        table._sorts = [];
                        table.saveOption("sorts");
                        for(var i in table.head.cells) {
                            table.head.cells[i].sort = 0;
                            table.head.cells[i].firstChild.hide();
                        }
                        if(table.filterInput) {
                            table.filter.clear();
                            table.filterInput.value = "";
                            table.filterInput.focus();
                            table.filterInput.apply();
                            table.filterInput.blur();
                        }
                        table.update();
                    }
                }, table);
            }

            if(options.filter == undefined || options.filter) {

                table.filterLayout = create(HTML.DIV, {
                    className: "table-filter"
                }, table);

                table.filterButton = create(HTML.DIV, {
                    className: "table-filter-button notranslate",
                    innerHTML: "search",
                    title: "Filter",
                    onclick: function() {
                        table.filterButton.hide();
                        table.filterInput.classList.remove("hidden");
                        table.filterInput.focus();
                    }
                }, table.filterLayout);

                table.filterInput = create(HTML.INPUT, {
                    className: "table-filter-input hidden",
                    tabindex: -1,
                    onkeyup: function(evt) {
                        if(evt.keyCode === 27) {
                            evt.preventDefault();
                            evt.stopPropagation();
                            if(this.value) {
                                this.value = "";
                            } else {
                                this.blur();
                            }
                        }
                        clearTimeout(table.filterInput.updateTask);
                        table.filterInput.updateTask = setTimeout(function(){
                            table.filterInput.apply();
                        }, 300);
                    },
                    onblur: function() {
                        if(!this.value) {
                            table.filterInput.classList.add("hidden");
                            table.filterButton.show();
                        }
                    },
                    onclick: function() {
                        this.focus();
                    },
                    _filter: function(row) {
                        var contains = false;
                        for(var i in row.cells) {
                            if(row.cells[i].innerText.toLowerCase().indexOf(this.filterInput.value.toLowerCase()) >= 0) return true;
                        }
                        return false;
                    },
                    apply: function() {
                        if(this.value) {
                            table.filterClear.show();
                        } else {
                            table.filterClear.hide();
                        }
                        var counter = 0;
                        table.filter.add(table.filterInput._filter);
                        table.filter();
                    }
                }, table.filterLayout);
                table.filterClear = create(HTML.DIV, {
                    className: "table-filter-clear hidden notranslate",
                    innerHTML: "clear",
                    onclick: function() {
                        table.filterInput.value = "";
                        table.filterInput.focus();
                        table.filterInput.apply();
                    }
                }, table.filterLayout);
            }

            function normalizeFunction(func) {
                if(!func) return null;
                save(":functemp", func);
                func = load(":functemp");
                save(":functemp");
                return func;
            }
            function checkIfFilterInList(filter) {
                if(!filter) return true;
                for(var i in table.filter.options) {
                    if(table.filter.options[i].toString() == filter.toString()) return i;
                }
                return -1;
            }

            table.filter.set = function(filterOption) {
                if(filterOption) {
                    table.filter.options = [normalizeFunction(filterOption)];
                } else {
                    table.filter.options = null;
                }
                table.saveOption("filter",table.filter.options);
                table.filter();
            };
            table.filter.add = function(filterOption) {
                table.filter.options = table.filter.options || [];
                var newFilterOption = normalizeFunction(filterOption);
                if(checkIfFilterInList(newFilterOption) < 0) {
                    table.filter.options.push(newFilterOption);
                }
                table.saveOption("filter",table.filter.options);
                table.filter();
            };
            table.filter.remove = function(filterOption) {
                table.filter.options = table.filter.options || [];
                var newFilterOption = normalizeFunction(filterOption);
                var index = checkIfFilterInList(newFilterOption);
                if(index >= 0) {
                    table.filter.options.splice(index,1);
                }
                table.saveOption("filter",table.filter.options);
                table.filter();
            };
            table.filter.clear = function() {
                table.filter.options = null;
                table.saveOption("filter",table.filter.options);
                table.filter();
            }
        }

        table.body = create(HTML.DIV, {className:"tbody" + optionalClassName(options.bodyClassName)}, table);

        table.placeholder = create(HTML.DIV, {
            className:"table-placeholder",
            innerHTML: options.placeholder || "No data",
            show: function(text){
//                clear(table.body);
                if(text) table.placeholder.innerHTML = text;
                table.placeholder.classList.remove("hidden");
            }
        }, table);

        if(options.id) {
            var savedOptions = load("table:" + options.id) || {};
            table.filter.options = savedOptions.filter;
            table._sorts = savedOptions.sorts || [];

            table.filter();
        }

        return table;
    }
    this.table = table;

    var loadingHolder;
    function loading(progress) {
        loadingHolder = loadingHolder || create("div", {style:{
            position: "fixed", top: 0, bottom: 0, left: 0, right: 0,
            zIndex: 10000, backgroundColor: "white", display: "flex", flexDirection: "column",
            justifyContent: "center", alignItems: "center", fontFamily: "sans-serif"
        }}, document.body)
            .place(HTML.DIV, {className:"loading-progress-circle"})
            .place(HTML.DIV, {className:"loading-progress-title", innerHTML: "Loading, please wait... "})
            .place(HTML.DIV, {className:"loading-progress-subtitle hidden"});
        if(progress) {
            lang.updateNode(loadingHolder.lastChild, progress);
            loadingHolder.lastChild.show();
        } else {
            loadingHolder.lastChild.hide();
        }
    }
    this.loading = loading;
    loading.hide = function() {
        loadingHolder.hide();
    };

    var progressHolder;
    /**
     * progress(options [, appendTo])
     * options = {
    *       label,
    *       className,
    *       dim: true|*false*,
    *   }
     * progress.show([label])
     * progress.hide()
     */
    function Progress(options, appendTo) {
        options = options || {};
        if(typeof options === "string") {
            options = { label: options };
        } else if(options instanceof HTMLSpanElement) {
            options.label = options.outerHTML;
        }
        options.label = options.label || "Loading...";
        options.dim = options.dim || false;

        appendTo = appendTo || document.body;

        progressHolder = progressHolder || dialog({
            className: "progress-dialog" + optionalClassName(options.className),
            items: [
                { type: HTML.DIV, className: "progress-dialog-circle" },
                { type: HTML.DIV, className: "progress-dialog-title" }
            ]
        }, appendTo)
//        progress.show(options.label);
    }
    Progress.prototype.show = function(label) {
        progressHolder.items[1].innerHTML = label || "Loading...";
        progressHolder.open();
    };
    Progress.prototype.hide = function() {
        progressHolder.close();
    };
    this.progress = new Progress();


    /**
     * eventBus.register(file, options) or eventBus.register(files, options)
     * options = {
    *       context,
    *       onprogress: function((int) loadedFiles)
    *       validate: function() -> true|false
    *       onstart: function(),
    *       onsuccess: function(),
    *       onerror: function(code, origin, error)
    *   }
     * eventBus.fire(event, object)
     * eventBus.fire(callback)
     *
     * File can be presented as the path, ".js" will be added if not exists.
     * File will be added as a holder if it is based on eventBus.eventHolder or
     * it has following elements:
     *   type: String
     *   onEvent: function(event, object)
     *   start: function()
     */
    function eventBus() {
        this.events = window.EVENTS = window.EVENTS || {};

        this.eventHolder = function() {
            return {
                onEvent:function(){console.warn("DEFINE onEvent(event, object)")},
//                start:function(){console.warn("DEFINE start()")},
                type:"DEFINE TYPE"
            }
        };

        var loaded = 0;
        this.origins = [];
        this.modules = [];
        this.holders = {};
        this.register = function(module, options) {
            var promise = new EPromise();
            if(module.constructor == Array) {
                for(var i in module) {
                    self.eventBus.register(module[i], options);
                }
            } else {
                self.eventBus.origins.push(module);
                var file = module;

                require(file, options.context).then(function(e) {
                    loaded++;
                    if(e && e.moduleName && e.type) {
//                        self.eventBus.holders[e.type.toLowerCase()] = e;
                        self.eventBus.holders[e.origin] = e;
                    }
                    if(options.onprogress) options.onprogress(loaded);

                    if(loaded == self.eventBus.origins.length) {
                        console.log("Preload finished: "+loaded+" files done.");

                        if(options.validate && !options.validate()) {
                            return;
                        }

                        if(options.modules) {
                            for(i in options.modules) {
                                if(window[options.modules[i]]) {
                                    self.eventBus.modules.push(options.modules[i].toLowerCase().replace("holder",""));
                                    self.eventBus.holders[options.modules[i].toLowerCase().replace("holder","")] = new window[options.modules[i]](options.context);
                                }
                            }
                        } else {
                            for(var i in self.eventBus.origins) {
                                var holder = self.eventBus.holders[self.eventBus.origins[i]];
                                if(holder && holder.type) {
                                    self.eventBus.modules.push(holder.type.toLowerCase());
                                    self.eventBus.holders[holder.type.toLowerCase()] = holder;
                                    delete self.eventBus.holders[holder.origin];
                                }
                            }
                        }
                        if(options.onstart) options.onstart(self.eventBus.modules);

                        for(i in self.eventBus.modules) {
                            if(self.eventBus.holders[self.eventBus.modules[i]].start) self.eventBus.holders[self.eventBus.modules[i]].start();
                        }

                        options.onsuccess();
                    }
                }).catch(function(code, e, error){
                    if(options.onerror) options.onerror(code, module, error);
                });
            }
        };

        this.fire = function(event, object) {
            if(!event) return;
            //setTimeout(function(){
                for(var i in self.eventBus.modules) {
                    var module = self.eventBus.modules[i];
                    if(self.eventBus.holders[module] && self.eventBus.holders[module].onEvent) {
                        try {
                            if(event.constructor === Function) {
                                var res = event.call(this, self.eventBus.holders[self.eventBus.modules[i]]);
                                if(res !== undefined && !res) break;
                            } else {
                                if (!self.eventBus.holders[module].onEvent.call(this, event, object)) break;
                            }
                        } catch(e) {
                            console.error(module, event, e);
                        }
                    }
                }
            //}.bind(this), 0);
        };

        this.chain = function(callback) {
            for(var i in self.eventBus.modules) {
                try{
                    var res = callback(self.eventBus.holders[self.eventBus.modules[i]]);
                    if(res !== undefined && !res) break;
                } catch(e) {
                    console.error(self.eventBus.modules[i], e);
                }
            }
        }
    }
    this.eventBus = new eventBus();
    this.fire = this.eventBus.fire;


    /**
     * menu
     */
    function menu(options) {
        options = options || {};
        options.className = "menu" + optionalClassName(options.className);
        options.tabindex = -1;
        //options.autoclose = true;

        //options._onopen = options.onopen;
        //options.onopen = function(evt) {
        //    console.log("MENU OPEN");
        //    if(options._onopen) options._onopen(evt);
        //};

        //options._onclose = options.onclose;
        //options.onclose = function(evt) {
        //    console.log("MENU CLOSE");
        //    if(options._onclose) options._onclose(evt);
        //};

        var items = options.items || [];
        options.items = [];
        var menu = new dialog(options, document.body);

        menu._addItem = menu.addItem;
        menu.addItem = function(item) {
            item._onclick = item.onclick;
            item.onclick = function(evt) {
                if(this._onclick) this._onclick(evt);
                menu.close(HIDING.OPACITY)
            };
            menu._addItem(item);
        };
        menu.addItems(items);

        menu._open = menu.open;
        menu.open = function(aroundNode) {
            menu._open(HIDING.SCALE_Y_BOTTOM);
            menu.style.top = (aroundNode.getBoundingClientRect().bottom + 5) + "px";
            menu.style.left = (aroundNode.getBoundingClientRect().right - menu.offsetWidth) + "px";
        };

        return menu;
    }
    this.menu = menu;


    function optionalClassName(className) {
        return className ? " " + className : "";
    }

    options = options || {};
    if(options.exportConstants) {
        window.HTML = HTML;
        window.ERRORS = ERRORS;
        window.DRAWER = DRAWER;
        window.HIDING = HIDING;
    }

    this.context = options.context || "";
    this.origin = options.origin || "edequate";

}
