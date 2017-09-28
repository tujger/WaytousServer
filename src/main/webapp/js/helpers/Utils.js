/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 * Author: Edward Mukhutdinov <wise@edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/19/17.
 */

function Utils(main) {

    function showAlert(text) {
        var div = document.getElementsByClassName("alert")[0];
        div.innerHTML = text;
        div.style.display = HTML.BLOCK;
    }

    function getHexColor(color){
        color >>>= 0;
        var b = color & 0xFF,
            g = (color & 0xFF00) >>> 8,
            r = (color & 0xFF0000) >>> 16;

        r = r.toString(16);if(r.length == 1) r = "0"+r;
        g = g.toString(16);if(g.length == 1) g = "0"+g;
        b = b.toString(16);if(b.length == 1) b = "0"+b;

        return "#"+r+g+b;
    }

    function getRGBAColor(color, alpha) {
        if(!color) return;
        if(color.constructor === String) {
            if(color.match(/^#/)) {
                color = color.replace("#","").split("");
                var r = parseInt(color[0]+color[1],16);
                var g = parseInt(color[2]+color[3],16);
                var b = parseInt(color[4]+color[5],16);
                color = (r*256 + g)*256 + b;
                if(alpha) {
                    color = "rgba("+r+", "+g+", "+b+", "+alpha+")";
                } else {
                    color = "rgb("+r+", "+g+", "+b+")";
                }
            }
        } else if (color.constructor === Number) {
            color >>>= 0;
            var b = color & 0xFF,
                g = (color & 0xFF00) >>> 8,
                r = (color & 0xFF0000) >>> 16,
                a = (( (color & 0xFF000000) >>> 24 ) / 255) || 1;
            if(alpha) a = alpha;
            color = "rgba(" + [r, g, b, a].join(",") + ")";
        }
        return color;
    }

    function getDecimalColor(color, alpha) {
        if(!color) return;
        if(color.constructor === String) {
            if(color.match(/^#/)) {
                color = color.replace("#","");
                color = parseInt(color,16);
            }
        }
        return color;
    }

    function getUuid(callback) {
        var uuid = u.load("uuid");

        if(!uuid) {
            if(callback) {
                new Fingerprint2().get(function(result, components){
                    console.log(result); //a hash, representing your device fingerprint
                    console.log(components); // an array of FP components
                    if(!result) {
                        var d = new Date().getTime();
                        result = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                            var r = (d + Math.random()*16)%16 | 0;
                            d = Math.floor(d/16);
                            return (c=='x' ? r : (r&0x3|0x8)).toString(16);
                        });
                    }
                    u.save("uuid", utils.getEncryptedHash(result));
                    callback();
                });
            } else {
                console.error("UUID not defined.");
            }
        }
        if(callback) callback();
        return uuid;
    }

    function jsonToLocation(json) {
        var loc = {};
        loc.coords = {};
        loc.provider = json[USER.PROVIDER];
        loc.coords.latitude = json[USER.LATITUDE];
        loc.coords.longitude = json[USER.LONGITUDE];
        loc.coords.altitude = json[USER.ALTITUDE] || null;
        loc.coords.accuracy = json[USER.ACCURACY] || null;
        loc.coords.heading = json[USER.BEARING] || null;
        loc.coords.speed = json[USER.SPEED] || null;
        loc.timestamp = json[REQUEST.TIMESTAMP];
        return loc;
    }

    function locationToJson(location) {
        var json = {};
        json[USER.PROVIDER] = location.provider || "fused";
        location.coords = location.coords || {};
        json[USER.LATITUDE] = location.coords.latitude;
        json[USER.LONGITUDE] = location.coords.longitude;
        json[USER.ALTITUDE] = location.coords.altitude || 0;
        json[USER.ACCURACY] = location.coords.accuracy || 50;
        json[USER.BEARING] = location.coords.heading || 0;
        json[USER.SPEED] = location.coords.speed || 0;
        json[REQUEST.TIMESTAMP] = location.timestamp || new Date().getTime();
        return json;
    }

    function latLng(location) {
        if(!location || !location.coords) return null;
        return new google.maps.LatLng(location.coords.latitude, location.coords.longitude);
    }

    function smoothInterpolated(duration, callback, postCallback) {
        var start = new Date().getTime();

        // var a = setInterval(function(){
        //     var t,v,elapsed;
        //     elapsed = new Date().getTime() - start;
        //     t = elapsed / duration;
        //     v = elapsed / duration;
        //     callback(t,v);
        // }, 16);
        // setTimeout(function() {
        //     clearInterval(a);
        //     if(postCallback) postCallback();
        // }, duration+1);
        postCallback();
        var a = null;
        return a;
    }

    function getEncryptedHash(s) {
        function L(k, d) {
            return (k << d) | (k >>> (32 - d))
        }

        function K(G, k) {
            var I, d, F, H, x;
            F = (G & 2147483648);
            H = (k & 2147483648);
            I = (G & 1073741824);
            d = (k & 1073741824);
            x = (G & 1073741823) + (k & 1073741823);
            if (I & d) {
                return (x ^ 2147483648 ^ F ^ H)
            }
            if (I | d) {
                if (x & 1073741824) {
                    return (x ^ 3221225472 ^ F ^ H)
                } else {
                    return (x ^ 1073741824 ^ F ^ H)
                }
            } else {
                return (x ^ F ^ H)
            }
        }

        function r(d, F, k) {
            return (d & F) | ((~d) & k)
        }

        function q(d, F, k) {
            return (d & k) | (F & (~k))
        }

        function p(d, F, k) {
            return (d ^ F ^ k)
        }

        function n(d, F, k) {
            return (F ^ (d | (~k)))
        }

        function u(G, F, aa, Z, k, H, I) {
            G = K(G, K(K(r(F, aa, Z), k), I));
            return K(L(G, H), F)
        }

        function f(G, F, aa, Z, k, H, I) {
            G = K(G, K(K(q(F, aa, Z), k), I));
            return K(L(G, H), F)
        }

        function D(G, F, aa, Z, k, H, I) {
            G = K(G, K(K(p(F, aa, Z), k), I));
            return K(L(G, H), F)
        }

        function t(G, F, aa, Z, k, H, I) {
            G = K(G, K(K(n(F, aa, Z), k), I));
            return K(L(G, H), F)
        }

        function e(G) {
            var Z;
            var F = G.length;
            var x = F + 8;
            var k = (x - (x % 64)) / 64;
            var I = (k + 1) * 16;
            var aa = Array(I - 1);
            var d = 0;
            var H = 0;
            while (H < F) {
                Z = (H - (H % 4)) / 4;
                d = (H % 4) * 8;
                aa[Z] = (aa[Z] | (G.charCodeAt(H) << d));
                H++
            }
            Z = (H - (H % 4)) / 4;
            d = (H % 4) * 8;
            aa[Z] = aa[Z] | (128 << d);
            aa[I - 2] = F << 3;
            aa[I - 1] = F >>> 29;
            return aa
        }

        function B(x) {
            var k = "", F = "", G, d;
            for (d = 0; d <= 3; d++) {
                G = (x >>> (d * 8)) & 255;
                F = "0" + G.toString(16);
                k = k + F.substr(F.length - 2, 2)
            }
            return k
        }

        function J(k) {
            k = k.replace(/rn/g, "n");
            var d = "";
            for (var F = 0; F < k.length; F++) {
                var x = k.charCodeAt(F);
                if (x < 128) {
                    d += String.fromCharCode(x)
                } else {
                    if ((x > 127) && (x < 2048)) {
                        d += String.fromCharCode((x >> 6) | 192);
                        d += String.fromCharCode((x & 63) | 128)
                    } else {
                        d += String.fromCharCode((x >> 12) | 224);
                        d += String.fromCharCode(((x >> 6) & 63) | 128);
                        d += String.fromCharCode((x & 63) | 128)
                    }
                }
            }
            return d
        }

        var C = Array();
        var P, h, E, v, g, Y, X, W, V;
        var S = 7, Q = 12, N = 17, M = 22;
        var A = 5, z = 9, y = 14, w = 20;
        var o = 4, m = 11, l = 16, j = 23;
        var U = 6, T = 10, R = 15, O = 21;
        s = J(s);
        C = e(s);
        Y = 1732584193;
        X = 4023233417;
        W = 2562383102;
        V = 271733878;
        for (P = 0; P < C.length; P += 16) {
            h = Y;
            E = X;
            v = W;
            g = V;
            Y = u(Y, X, W, V, C[P + 0], S, 3614090360);
            V = u(V, Y, X, W, C[P + 1], Q, 3905402710);
            W = u(W, V, Y, X, C[P + 2], N, 606105819);
            X = u(X, W, V, Y, C[P + 3], M, 3250441966);
            Y = u(Y, X, W, V, C[P + 4], S, 4118548399);
            V = u(V, Y, X, W, C[P + 5], Q, 1200080426);
            W = u(W, V, Y, X, C[P + 6], N, 2821735955);
            X = u(X, W, V, Y, C[P + 7], M, 4249261313);
            Y = u(Y, X, W, V, C[P + 8], S, 1770035416);
            V = u(V, Y, X, W, C[P + 9], Q, 2336552879);
            W = u(W, V, Y, X, C[P + 10], N, 4294925233);
            X = u(X, W, V, Y, C[P + 11], M, 2304563134);
            Y = u(Y, X, W, V, C[P + 12], S, 1804603682);
            V = u(V, Y, X, W, C[P + 13], Q, 4254626195);
            W = u(W, V, Y, X, C[P + 14], N, 2792965006);
            X = u(X, W, V, Y, C[P + 15], M, 1236535329);
            Y = f(Y, X, W, V, C[P + 1], A, 4129170786);
            V = f(V, Y, X, W, C[P + 6], z, 3225465664);
            W = f(W, V, Y, X, C[P + 11], y, 643717713);
            X = f(X, W, V, Y, C[P + 0], w, 3921069994);
            Y = f(Y, X, W, V, C[P + 5], A, 3593408605);
            V = f(V, Y, X, W, C[P + 10], z, 38016083);
            W = f(W, V, Y, X, C[P + 15], y, 3634488961);
            X = f(X, W, V, Y, C[P + 4], w, 3889429448);
            Y = f(Y, X, W, V, C[P + 9], A, 568446438);
            V = f(V, Y, X, W, C[P + 14], z, 3275163606);
            W = f(W, V, Y, X, C[P + 3], y, 4107603335);
            X = f(X, W, V, Y, C[P + 8], w, 1163531501);
            Y = f(Y, X, W, V, C[P + 13], A, 2850285829);
            V = f(V, Y, X, W, C[P + 2], z, 4243563512);
            W = f(W, V, Y, X, C[P + 7], y, 1735328473);
            X = f(X, W, V, Y, C[P + 12], w, 2368359562);
            Y = D(Y, X, W, V, C[P + 5], o, 4294588738);
            V = D(V, Y, X, W, C[P + 8], m, 2272392833);
            W = D(W, V, Y, X, C[P + 11], l, 1839030562);
            X = D(X, W, V, Y, C[P + 14], j, 4259657740);
            Y = D(Y, X, W, V, C[P + 1], o, 2763975236);
            V = D(V, Y, X, W, C[P + 4], m, 1272893353);
            W = D(W, V, Y, X, C[P + 7], l, 4139469664);
            X = D(X, W, V, Y, C[P + 10], j, 3200236656);
            Y = D(Y, X, W, V, C[P + 13], o, 681279174);
            V = D(V, Y, X, W, C[P + 0], m, 3936430074);
            W = D(W, V, Y, X, C[P + 3], l, 3572445317);
            X = D(X, W, V, Y, C[P + 6], j, 76029189);
            Y = D(Y, X, W, V, C[P + 9], o, 3654602809);
            V = D(V, Y, X, W, C[P + 12], m, 3873151461);
            W = D(W, V, Y, X, C[P + 15], l, 530742520);
            X = D(X, W, V, Y, C[P + 2], j, 3299628645);
            Y = t(Y, X, W, V, C[P + 0], U, 4096336452);
            V = t(V, Y, X, W, C[P + 7], T, 1126891415);
            W = t(W, V, Y, X, C[P + 14], R, 2878612391);
            X = t(X, W, V, Y, C[P + 5], O, 4237533241);
            Y = t(Y, X, W, V, C[P + 12], U, 1700485571);
            V = t(V, Y, X, W, C[P + 3], T, 2399980690);
            W = t(W, V, Y, X, C[P + 10], R, 4293915773);
            X = t(X, W, V, Y, C[P + 1], O, 2240044497);
            Y = t(Y, X, W, V, C[P + 8], U, 1873313359);
            V = t(V, Y, X, W, C[P + 15], T, 4264355552);
            W = t(W, V, Y, X, C[P + 6], R, 2734768916);
            X = t(X, W, V, Y, C[P + 13], O, 1309151649);
            Y = t(Y, X, W, V, C[P + 4], U, 4149444226);
            V = t(V, Y, X, W, C[P + 11], T, 3174756917);
            W = t(W, V, Y, X, C[P + 2], R, 718787259);
            X = t(X, W, V, Y, C[P + 9], O, 3951481745);
            Y = K(Y, h);
            X = K(X, E);
            W = K(W, v);
            V = K(V, g)
        }
        var i = B(Y) + B(X) + B(W) + B(V);
        return i.toLowerCase()
    }

    function formatLengthToLocale(meters) {
        if(navigator.language && navigator.language.toLowerCase().slice(0,2) == "en") {
            meters = meters * 3.2808399;
            if(meters < 530) {
                return "%s %s".sprintf(meters.toFixed(0), "ft");
            } else {
                meters = meters / 5280;
                return "%s %s".sprintf(meters.toFixed(1), "mi");
            }
        } else {
            var unit = "m";
            if (meters < 1) {
                meters *= 1000;
                unit = "mm";
            } else if (meters > 1000) {
                meters /= 1000;
                unit = "km";
            }
            return "%s %s".sprintf(meters.toFixed(1), unit);
        }
    }

    var label = function(options, node) {
        // Initialization
        this.setValues(options);

        // Label specific
        if(!node) {
            node = u.create(HTML.DIV, {className:options.className});
            if(options.style) {
                if(options.style.constructor !== String) {
                    var s = "";
                    for(var x in options.style) {
                        if(options.style.hasOwnProperty(x)) {
                            s += u.normalizeName(x) + ":" + options.style[x] + ";";
                        }
                    }
                    options.style = s;
                }
                node.setAttribute("style", options.style);
            }
        }
        this.span_ = node;
        var div = this.div_ = u.create(HTML.DIV, {style: "position: absolute; display: none"});
        div.appendChild(node);

        this.onAdd = function() {
            var pane = this.getPanes().overlayLayer;
            pane.appendChild(this.div_);

            // Ensures the label is redrawn if the text or position is changed.
            var me = this;
            this.listeners_ = [
                google.maps.event.addListener(this, "position_changed", function() { me.draw(); }),
                google.maps.event.addListener(this, "text_changed", function() { me.draw(); })
            ];
        };
        this.draw = function() {
            var projection = this.getProjection();
            var position = projection.fromLatLngToDivPixel(this.get('position'));

            if(position) {
                var div = this.div_;
                div.style.left = position.x + "px";
                div.style.top = position.y + "px";
                div.style.display = HTML.BLOCK;

                this.span_.innerHTML = this.get("text").toString();
            }
        };
        this.onRemove = function() {
            this.div_.parentNode.removeChild(this.div_);

            // Label is removed from the map, stop updating its position/text.
            for (var i = 0, I = this.listeners_.length; i < I; ++i) {
                google.maps.event.removeListener(this.listeners_[i]);
            }
        };
    };

    function findPoint(points, fraction) {
        var length = 0;
        fraction = fraction || .5;
        for(var i in points) {
            if(i == 0) continue;
            length += google.maps.geometry.spherical.computeDistanceBetween(points[i-1], points[i]);
        }
        length = length * fraction;

        for(var i in points) {
            if(i == 0) continue;
            var current = google.maps.geometry.spherical.computeDistanceBetween(points[i-1], points[i]);
            if(length - current < 0) {
                return google.maps.geometry.spherical.interpolate(points[i-1], points[i], length / current);
            } else {
                length -= current;
            }
        }
        return google.maps.geometry.spherical.interpolate(points[0], points[points.length -1], fraction);
    }

    function reduce(bounds, fraction) {
        var newNortheast = google.maps.geometry.spherical.interpolate(bounds.getNorthEast(), bounds.getSouthWest(), (1+fraction)/2);
        var newSouthwest = google.maps.geometry.spherical.interpolate(bounds.getSouthWest(), bounds.getNorthEast(), (1+fraction)/2);
        bounds = new google.maps.LatLngBounds();
        bounds.extend(newNortheast);
        bounds.extend(newSouthwest);
        return bounds;
    }

    var popupBlockerChecker = {
        check: function(popup_window, onblocked){
            var _scope = this;
            if(onblocked) this._displayError = onblocked;
            if (popup_window) {
                if(/chrome/.test(navigator.userAgent.toLowerCase())){
                    setTimeout(function () {
                        _scope._is_popup_blocked(_scope, popup_window);
                    },200);
                }else{
                    popup_window.onload = function () {
                        _scope._is_popup_blocked(_scope, popup_window);
                    };
                }
            }else{
                _scope._displayError();
            }
        },
        _is_popup_blocked: function(scope, popup_window){
            if ((popup_window.innerHeight > 0)==false){ scope._displayError(); }
        },
        _displayError: function(){
            console.log("Popup Blocker is enabled! Please add this site to your exception list.");
        }
    };

    function dialogAbout(appendTo) {
        return u.dialog({
            className: "about-dialog",
            itemsClassName: "about-dialog-items",
            buttonsClassName: "about-dialog-buttons",
            items: [
                { innerHTML: "${APP_NAME} v.1.${SERVER_BUILD}" },
                { content: u.create(HTML.DIV)
                    .place(HTML.A, { className: "about-dialog-link", href: "/", target: "_blank", rel:"noopener", innerHTML: u.lang.support || "Home"})
                    .place(HTML.A, { className: "about-dialog-link", href: "/support/", target: "_blank",rel:"noopener", innerHTML: u.lang.support || "Support"})
                    .place(HTML.A, { className: "about-dialog-link", href: "/help/", target: "_blank", rel:"noopener", innerHTML: u.lang.help || "Help"})
                    .place(HTML.A, { className: "about-dialog-link", href: "/feedback/", target: "_blank", rel:"noopener", innerHTML: u.lang.feedback || "Feedback" })
                },
                { innerHTML: "&nbsp;" },
                { content: [
                    u.create(HTML.IMG, {src: "/images/edeqa-logo.svg", className: "about-dialog-edeqa-logo"}),
                    u.create(HTML.DIV)
                        .place(HTML.DIV, { innerHTML: "Copyright &copy;2017 Edeqa" })
                        .place(HTML.A, {className: "about-dialog-edeqa-link", href: "http://www.edeqa.com", target: "_blank", rel:"noopener", innerHTML: "http://www.edeqa.com" })
                ]
                },
                /*
                               { innerHTML: "Copyright &copy;2017 Edeqa" },
                               { type: HTML.A, className: "about-dialog-edeqa-link", href: "http://www.edeqa.com", target: "_blank", rel:"noopener", innerHTML: "http://www.edeqa.com" },
                */
                { enclosed: true, label: u.lang.legal_information || "Legal information", body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum." },
            ],
            positive: {
                label: u.lang.ok
            }
        });
    }

    function labelPosition(map, points, mePosition, userPosition) {

        var markerPosition = utils.findPoint(points);
        var bounds = map.getBounds();
        var reducedBounds = reduce(map.getBounds(), 0.8);

        if(!reducedBounds.contains(markerPosition) && (bounds.contains(mePosition) || bounds.contains(userPosition))) {
            if(!reducedBounds.contains(markerPosition)) {
                var fract = 0.5;
                while(!reducedBounds.contains(markerPosition)) {
                    fract = fract + (bounds.contains(mePosition) ? -1 : +1) * .01;
                    if (fract < 0 || fract > 1) break;
                    markerPosition = findPoint(points, fract);
                }
            }
        }
        return markerPosition;
    }

    function toDateString(millis) {
        var res = "";
        var delta = parseInt(millis/1000);

        var secInDay = 24 * 60 * 60;
        var secInHour = 24 * 60;
        var secInMinute = 60;

        var d = parseInt(delta / secInDay);
        delta -= d * secInDay;

        var h = parseInt(delta / secInHour);
        delta -= h * secInHour;

        var m = parseInt(delta / secInMinute);
        var s = delta - m * secInMinute;

        if(d) {
            res += d + "d";
        }
        if(h) {
            if(res) res += " ";
            res += h + "h";
        }
        if(m) {
            if(res) res += " ";
            res += m + "m";
        }
        if(res) res += " ";
        res += (s ? s : "0") + "s";
        return res;
    }

    function brightness(r, g, b){
        return (r * 299 + g * 587 + b * 114) / 1000
    }

    function Sync(options) {
        options = options || {};

        try {
            var data = firebase.auth().currentUser;
            if(data) {
                options.uid = options.uid || data.uid;
            }
        } catch(e) {}

        options.ongetvalue = options.ongetvalue || function(key, value) {
            console.warn("Got value: " + key, value);
            return value;
        };
        options.onupdatevalue = options.onupdatevalue || function(key, newValue, oldValue) {
            console.warn("Updated value: " + key, "[new]:", newValue, "[old]:", oldValue);
        };
        options.onerror = options.onerror || function(key, error) {
            console.error("Error: " + key, error);
        };
        options.reference = options.reference || database.ref();
        options.mode = Sync.Mode.UPDATE_BOTH;

        this.setReference = function(ref) {
            options.reference = ref;
        };
        this.setKey = function(key) {
            options.key = key;
        };
        this.setUid = function(uid) {
            options.uid = uid;
        };
        this.setType = function(type) {
            options.type = type;
        };
        this.setOnGetValue = function(callback) {
            options.ongetvalue = callback;
        };
        this.setOnUpdateValue = function(callback) {
            options.onupdatevalue = callback;
        };
        this.setOnError = function(callback) {
            options.onerror = callback;
        };
        this.setGroup = function(group) {
            options.group = group;
        };
        this.setMode = function(mode) {
            options.mode = mode;
        };

        this._getValue = function(key, ongetvalue, onerror) {
            if(!key) {
                console.error("Key not defined.");
                return;
            }
            var ref = this._getRef();;

            key = key.split("/");
            if(!key[key.length - 1] || key[key.length - 1] == "null" || key[key.length - 1] == "undefined") {
                key[key.length - 1] = options.reference.push().key;
            }
            key = key.join("/");

            if(ref) {
                var lastKey;
                var onsuccess = function (data) {
                    var val = data.val();
                    ongetvalue(data.key, val);
                };
                var onfail = function (error) {
                    onerror(key, error);
                };
                ref.once("value").then(onsuccess).catch(onfail);
            }
        };

        this.getValue = function() {
            this._getValue(options.key, options.ongetvalue, options.onerror);
        };

        this._syncValue = function(mode, newValue, ongetvalue, onupdatevalue, onerror) {
            var ref = this._getRef();
            if(!ref) return;
            this._getValue(options.key, function(key, value) {
                var updates = {};
                var resolvedValue = newValue;
                if(resolvedValue && resolvedValue.constructor === Object) {
                    resolvedValue = resolvedValue[key];
                }
                if(resolvedValue === undefined) {
                    resolvedValue = ongetvalue(key, value);
                }
                if(resolvedValue === undefined) {
                    onerror(key, "NewValue not defined, define it or use 'ongetvalue'.");
                    return;
                }
                switch(mode) {
                    case Sync.Mode.UPDATE_LOCAL:
                        if(!resolvedValue && value != resolvedValue) {
                            onupdatevalue(key, value, resolvedValue);
                        }
                        return;
                    case Sync.Mode.OVERRIDE_LOCAL:
                        if(value != resolvedValue) {
                            onupdatevalue(key, value, resolvedValue);
                        }
                        return;
                    case Sync.Mode.UPDATE_REMOTE:
                        if(!value && value !== resolvedValue) {
                            updates[key] = resolvedValue;
                            ref.update(updates).then(function () {
                                onupdatevalue(key, resolvedValue, value);
                            }).catch(function (error) {
                                onerror(key, error);
                            });
                        }
                        break;
                    case Sync.Mode.OVERRIDE_REMOTE:
                        if(value != resolvedValue) {
                            updates[key] = resolvedValue;
                            ref.update(updates).then(function () {
                                onupdatevalue(key, resolvedValue, value);
                            }).catch(function (error) {
                                onerror(key, error);
                            });
                        }
                        break;
                    case Sync.Mode.UPDATE_BOTH:
                        if(!value && resolvedValue) {
                            updates[key] = resolvedValue;
                            ref.update(updates).then(function () {
                                onupdatevalue(key, resolvedValue, value);
                            }).catch(function (error) {
                                onerror(key, error);
                            });
                        } else if(value && !resolvedValue) {
                            onupdatevalue(key, value, resolvedValue);
                        }
                        break;
                    default:
                        onerror(key, "Mode not defined");
                        break;
                }
            }, onerror);
        };

        this.syncValue = function(value) {
            this._syncValue(false, Sync.Mode.UPDATE_BOTH, value, options.ongetvalue, options.onupdatevalue, options.onerror);
        };

        this.getValues = function() {
            var ref = this._getRef();
            if(!ref) return;

            var onsuccess = function (data) {
                var val = data.val();
                if (val && val.constructor === Array) {
                    for (var i in val) {
                        ongetvalue(data.key, val[i]);
                    }
                } else if (val && val.constructor === Object) {
                    for (var i in val) {
                        ongetvalue(data.key + "/" + i, val[i]);
                    }
                } else {
                ongetvalue(data.key, val);
                }
                if (data.key == lastKey) {
                    ref.off();
                }
            };

            ref.limitToLast(1).once("child_added").then(function (data) {
                var lastKey = data.key;
                if (!lastKey) return;

                ref.on("child_added", onsuccess, onfail);
            }).catch(onfail);
//            } else {
//                this._getValue(options.key, options.ongetvalue, options.onerror);
//            }
        };

        this.syncValues = function(values) {
            if(values && values.constructor === Object) {
                for(var x in values) {
                    var sync = new Sync({
                        type: options.type,
                        key: options.key + "/" + x,
//                        onupdatevalue: function(key, newName, oldName) {
//                            console.log(key, newName, oldName)
//                        }
                    });
                    sync.syncValue(values[x]);
                }
            } else if(!values) {
                this._syncValue(true, Sync.Mode.UPDATE_BOTH, undefined, options.ongetvalue, options.onupdatevalue, options.onerror);
            } else {
                options.onerror(options.key, "Incorrect values");
            }
        };

        this.setRemoteValueIfNull = function(value) {
            this._syncValue(false, Sync.Mode.UPDATE_REMOTE, value, options.ongetvalue, options.onupdatevalue, options.onerror);
        }

        this.setRemoteValue = function(value) {
            this._syncValue(false, Sync.Mode.OVERRIDE_REMOTE, value, options.ongetvalue, options.onupdatevalue, options.onerror);
        }

        this.setLocalValue = function(value) {
            this._syncValue(false, Sync.Mode.OVERRIDE_LOCAL, value, options.ongetvalue, options.onupdatevalue, options.onerror);
        }

        this.setLocalValueIfNull = function(value) {
            this._syncValue(false, Sync.Mode.UPDATE_LOCAL, value, options.ongetvalue, options.onupdatevalue, options.onerror);
        }

        this._getRef = function() {
            var ref;
            switch(options.type) {
                case Sync.Type.ACCOUNT_PRIVATE:
                    if(!options.uid) {
                        console.error("UID not defined.");
                        return;
                    } else {
                        ref = options.reference.child(DATABASE.SECTION_USERS).child(options.uid).child(DATABASE.PRIVATE);
                    }
                    break;
                case Sync.Type.USER_PUBLIC:
                    if(!options.group) {
                        console.error("Group not defined.");
                        return;
                    }
                    if(!options.userNumber) {
                        console.error("UserNumber not defined.");
                        return;
                    }
                    ref = options.reference.child(options.group).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(options.userNumber);
                    break;
            }
            if(!ref) {
                onerror(key, "Firebase database reference not defined.");
                return;
            }
        }

    }
    Sync.Type = {
        ACCOUNT_PRIVATE: "account-private",
        USER_PUBLIC: "user-public"
    };
    Sync.Mode = {
        UPDATE_REMOTE: "update-remote",
        OVERRIDE_REMOTE: "override-remote",
        UPDATE_LOCAL: "update-local",
        OVERRIDE_LOCAL: "override-local",
        UPDATE_BOTH: "update-both",
        SKIP: "skip"
    }
    Sync.CREATE_KEY = "$create_key";


    return {
        showAlert: showAlert,
        brightness: brightness,
        getHexColor: getHexColor,
        getRGBAColor:getRGBAColor,
        getDecimalColor:getDecimalColor,
        getUuid:getUuid,
        getEncryptedHash:getEncryptedHash,
        latLng:latLng,
        jsonToLocation:jsonToLocation,
        locationToJson:locationToJson,
        smoothInterpolated:smoothInterpolated,
        formatLengthToLocale:formatLengthToLocale,
        label:label,
        findPoint:findPoint,
        reduce:reduce,
        popupBlockerChecker:popupBlockerChecker,
        dialogAbout:dialogAbout,
        labelPosition:labelPosition,
        toDateString:toDateString,
        sync:Sync,
    }
}
