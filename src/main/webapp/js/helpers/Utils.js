/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 * Author: Edward Mukhutdinov <wise@edeqa.com>
 *
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

        r = r.toString(16);if(r.length === 1) r = "0"+r;
        g = g.toString(16);if(g.length === 1) g = "0"+g;
        b = b.toString(16);if(b.length === 1) b = "0"+b;

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
                            return (c==='x' ? r : (r&0x3|0x8)).toString(16);
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
        if(navigator.language && navigator.language.toLowerCase().slice(0,2) === "en") {
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
            if (popup_window.innerHeight <= 0){ scope._displayError(); }
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
                { innerHTML: "${APP_NAME} v.${SERVER_VERSION}.${SERVER_BUILD}" },
                { className: "about-dialog-link", innerHTML: "(%s)".sprintf(new Date(1542346572124).toDateString()) },
                { content: u.create(HTML.DIV)
                    .place(HTML.A, { className: "about-dialog-link", href: "/", target: "_blank", rel:"noopener", innerHTML: u.lang.home || "Home"})
                    .place(HTML.A, { className: "about-dialog-link", href: "/main/support/", target: "_blank",rel:"noopener", innerHTML: u.lang.support || "Support"})
                    .place(HTML.A, { className: "about-dialog-link", href: "/main/help/", target: "_blank", rel:"noopener", innerHTML: u.lang.help || "Help"})
                    .place(HTML.A, { className: "about-dialog-link", href: "/main/feedback/", target: "_blank", rel:"noopener", innerHTML: u.lang.feedback || "Feedback" })
                },
                { innerHTML: "&nbsp;" },
                { content: [
                    u.create(HTML.IMG, {src: "/images/edeqa-logo.svg", className: "about-dialog-edeqa-logo"}),
                    u.create(HTML.DIV)
                        .place(HTML.DIV, { innerHTML: "Copyright &copy;2017-18 Edeqa" })
                        .place(HTML.A, {className: "about-dialog-edeqa-link", href: "http://www.edeqa.com", target: "_blank", rel:"noopener", innerHTML: "http://www.edeqa.com" })
                ]},
                {
                    enclosed: true,
                    label: u.lang.legal_information || "Legal information",
                    body: u.lang.loading && u.lang.loading.outerHTML || "Loading...",
                    className: "dialog-about-terms",
                    onopen: function(e) {
                        var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                        u.post("/rest/content", {resource: "legal-information.html", locale: lang}).then(function(xhr){
                            e.body.innerHTML = xhr.response;
                        }).catch(function(error, json) {
                            e.body.innerHTML = u.lang.error;
                        });
                    }
                }
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

        options.debug = options.debug || true;
        options.ongetvalue = options.ongetvalue || function(key, value) {
            if(options.debug) console.warn("Got value: " + key, value);
            //return value;
        };
        options.onaddremotevalue = options.onaddremotevalue || function(key, value) {
//            if(options.debug) console.warn("Added remote: " + key, "[value]:", value);
        };
        options.onupdateremotevalue = options.onupdateremotevalue || function(key, newValue, oldValue) {
//            if(options.debug) console.warn("Updated remote: " + key, "[new]:", newValue, "[old]:", oldValue);
        };
        options.onremoveremotevalue = options.onremoveremotevalue || function(key, value) {
            if(options.debug) console.warn("Removed remote: " + key, "[value]:", value);
        };
        options.onsaveremotevalue = options.onsaveremotevalue || function(key, newValue, oldValue) {
            //if(options.debug) console.warn("Saved remote: " + key, "[new]:", newValue, "[old]:", oldValue);
        };
        options.onaddlocalvalue = options.onaddlocalvalue || function(key, value) {
//            if(options.debug) console.warn("Added local: " + key, "[value]:", value);
        };
        options.onupdatelocalvalue = options.onupdatelocalvalue || function(key, newValue, oldValue) {
//            if(options.debug) console.warn("Updated local: " + key, "[new]:", newValue, "[old]:", oldValue);
        };
        options.onremovelocalvalue = options.onremovelocalvalue || function(key, value) {
            if(options.debug) console.warn("Removed local: " + key, "[value]:", value);
        };
        options.onsavelocalvalue = options.onsavelocalvalue || function(key, newValue, oldValue) {
            //if(options.debug) console.warn("Saved local: " + key, "[new]:", newValue, "[old]:", oldValue);
        };
        options.onfinish = options.onfinish || function(key, value) {
        };
        options.onerror = options.onerror || function(key, error) {
            console.error("Error: " + key, error);
        };
        options.reference = options.reference || database.ref();
        options.mode = Sync.Mode.UPDATE_BOTH;
        options.log = options.log || false;

        this.setReference = function(ref) {
            options.reference = ref;
        };
        this.setKey = function(key) {
            options.key = key;
        };
        this.setChild = function(key) {
            options.child = child;
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
        this.setOnAddRemoteValue = function(callback) {
            options.onaddremotevalue = callback;
        };
        this.setOnUpdateRemoteValue = function(callback) {
            options.onupdateremotevalue = callback;
        };
        this.setOnRemoveRemoteValue = function(callback) {
            options.onremoveremotevalue = callback;
        };
        this.setOnAddLocalValue = function(callback) {
            options.onaddlocalvalue = callback;
        };
        this.setOnUpdateLocalValue = function(callback) {
            options.onupdatelocalvalue = callback;
        };
        this.setOnRemoveLocalValue = function(callback) {
            options.onremovelocalvalue = callback;
        };
        this.setOnError = function(callback) {
            options.onerror = callback;
        };
        this.setOnFinish = function(callback) {
            options.onfinish = callback;
        };
        this.setGroup = function(group) {
            options.group = group;
        };
        this.setMode = function(mode) {
            options.mode = mode;
        };
        this.setLog = function(log) {
            options.log = log;
        };

        this._getValue = function(key, ongetvalue, onfinish, onerror) {
            if(!key) {
                onerror(key, "Key not defined.");
                return;
            }

            var onsuccess = function (data) {
                var val = data.val();
                ongetvalue(data.key, val);
                if(onfinish) onfinish(options.mode, data.key, val);
            };
            var onfail = function (error) {
                onerror(key, error);
                if(onfinish) onfinish(options.mode, data.key);
            };
            this._ref.child(key).once("value").then(onsuccess).catch(onfail);
        };

        this.getValue = function() {
            this._ref = getRef(options.child);
            if(!this._ref) return;
            this._getValue(options.key, options.ongetvalue, options.onfinish, options.onerror);


        };

        this.getValues = function() {
            var self = this;
            self._ref = getRef(options.child);
            if(!self._ref) return;

            self._ref.child(options.key).limitToLast(1).once("child_added").then(function (data) {
                var lastKey = data.key;
                if (!lastKey) {
                    if(options.onfinish) options.onfinish(options.key, "No records");
                    return;
                }

                self._ref.child(options.key).on("child_added", function(data) {
                    options.ongetvalue(data.key, data.val());

                    if (data.key === lastKey) {
                        self._ref.child(options.key).off();
                        options.onfinish(Sync.Mode.GET_REMOTE, options.key);
                    }
                }, function (error) {
                    options.onerror(key, error);

                    if (data.key === lastKey) {
                        self._ref.child(options.key).off();
                        options.onfinish(Sync.Mode.GET_REMOTE, options.key);
                    }
                });
            }).catch(function (error) {
                options.onerror(key, error);
            });
        };

        this._syncValue = function(mode, newValue, ongetvalue, onaddremotevalue, onupdateremotevalue, onremoveremotevalue, onsaveremotevalue, onaddlocalvalue, onupdatelocalvalue, onremovelocalvalue, onsavelocalvalue, onfinish, onerror) {
            var self = this;
            this._getValue(options.key, function(key, remote) {

                var updates = {};
                var local = newValue;
                if(local === undefined) {
                    local = ongetvalue(key, remote);
                }
                if(local === undefined) {
                    onerror(key, "Local value not defined, define it or use 'ongetvalue'.");
                    return;
                }
                if(remote && local && remote.constructor !== local.constructor) {
                    onerror(key, "Remote value [" + (remote ? remote.constructor.name : null) +"] is not equivalent to local value [" + (local ? local.constructor.name : null) + "], use 'syncValues' for sync objects.");
                    return;
                }
                if(remote === local) return;

                switch(mode) {
                    case Sync.Mode.UPDATE_LOCAL:
                        var process = false;
                        if(!remote) {
                        } else if(remote && !local) {
                            process = true;
                        } else {
                            if(local.constructor === Object && local[DATABASE.SYNCED] && (local[DATABASE.SYNCED] === firebase.database.ServerValue.TIMESTAMP || local[DATABASE.SYNCED] < remote[DATABASE.SYNCED])) {
                                process = true;
                            }
                        }
                        if(process) {
                            if(local) {
                                onupdatelocalvalue(key, remote, local);
                                onsavelocalvalue(key, remote, local);
                                onfinish(Sync.Mode.UPDATE_LOCAL, key, remote, local);
                            } else {
                                onaddlocalvalue(key, remote);
                                onsavelocalvalue(key, remote);
                                onfinish(Sync.Mode.ADD_LOCAL, key, remote);
                            }
                        }
                        return;
                    case Sync.Mode.OVERRIDE_LOCAL:
                        if((local && local.constructor !== String) || (remote && remote.constructor !== String)) {
                            onerror(key, "Mode OVERRIDE_REMOTE allowed only for strings.");
                            return;
                        }
                        if(local) {
                            onupdatelocalvalue(key, remote, local);
                            onsavelocalvalue(key, remote, local);
                            onfinish(Sync.Mode.OVERRIDE_LOCAL, key, remote, local);
                        } else {
                            onaddlocalvalue(key, remote);
                            onsavelocalvalue(key, remote);
                            onfinish(Sync.Mode.ADD_LOCAL, key, remote);
                        }
                        return;
                    case Sync.Mode.UPDATE_REMOTE:
                        var process = false;
                        if(!remote && local) {
                            if(local.constructor === Object) {
                                local[DATABASE.SYNCED] = firebase.database.ServerValue.TIMESTAMP;
                            }
                            process = true;
                        } else if(remote && !local) {
                        } else {
                            if(local.constructor === Object && (!local[DATABASE.SYNCED] || local[DATABASE.SYNCED] === firebase.database.ServerValue.TIMESTAMP || local[DATABASE.SYNCED] > remote[DATABASE.SYNCED])) {
                                local[DATABASE.SYNCED] = firebase.database.ServerValue.TIMESTAMP;
                                process = true;
                            }
                        }
                        if(process) {
                            updates[key] = local;
                            self._ref.update(updates).then(function () {
                                self._getValue(options.key, function(key, updated) {
                                    if(remote) {
                                        onupdateremotevalue(key, updated, remote);
                                        onsaveremotevalue(key, updated, remote);
                                        onfinish(Sync.Mode.UPDATE_REMOTE, key, updated, remote);
                                        registerHistory.call(self, Sync.Mode.UPDATE_REMOTE, self._ref.key + "/" + key, updated)
                                    } else {
                                        onaddremotevalue(key, updated);
                                        onsaveremotevalue(key, updated);
                                        onfinish(Sync.Mode.ADD_REMOTE, key, updated);
                                        registerHistory.call(self, Sync.Mode.ADD_REMOTE, self._ref.key + "/" + key, updated)
                                    }
                                }, null, onerror);
                            }).catch(onerror);
                        }
                        break;
                    case Sync.Mode.OVERRIDE_REMOTE:
                        if((local && !(local.constructor === String || local.constructor === Boolean || local.constructor === Number))
                            || (remote && !(remote.constructor === String || remote.constructor === Boolean || remote.constructor === Number))) {
                            onerror(key, "Mode OVERRIDE_REMOTE allowed only for primitives (string, number, boolean).");
                            return;
                        }
                        if(local && local.constructor === Object) local[DATABASE.SYNCED] = firebase.database.ServerValue.TIMESTAMP;
                        updates[key] = local;
                        self._ref.update(updates).then(function () {
                            self._getValue(options.key, function(key, updated) {
                                if(remote) {
                                    onupdateremotevalue(key, updated, remote);
                                    onsaveremotevalue(key, updated, remote);
                                    onfinish(Sync.Mode.OVERRIDE_REMOTE, key, updated, remote);
                                    registerHistory.call(self, Sync.Mode.OVERRIDE_REMOTE, self._ref.key + "/" + key, updated)
                                } else {
                                    onaddremotevalue(key, updated);
                                    onsaveremotevalue(key, updated);
                                    onfinish(Sync.Mode.ADD_REMOTE, key, updated);
                                    registerHistory.call(self, Sync.Mode.ADD_REMOTE, self._ref.key + "/" + key, updated)
                                }
                            }, null, onerror);
                        }).catch(onerror);
                        break;
                    case Sync.Mode.UPDATE_BOTH:
                        var processLocal = false;
                        var processRemote = false;
                        if((remote === undefined || remote === null) && local !== undefined && local !== null) {
                            processRemote = true;
                        } else if(remote !== undefined && remote !== null && (local === undefined || local === null)) {
                            processLocal = true;
                        } else {
                            var localTimestamp = 0;
                            if(local.constructor === Object) {
                                localTimestamp = local[DATABASE.SYNCED];
                            } else {
                                localTimestamp = u.load("synced:" + options.type) || 0;
                            }
                            var remoteTimestamp = 0;
                            if(remote.constructor === Object) {
                                remoteTimestamp = remote[DATABASE.SYNCED];
                            } else {
                                var ref = getRef(DATABASE.SYNCED).toString();
                                if(Sync._specialWatch && Sync._specialWatch[ref]) {
                                    remoteTimestamp = Sync._specialWatch[ref].timestamp || 0;
                                }
                            }

                            if(!localTimestamp) {
                                processRemote = true;
                            } else if(localTimestamp > remoteTimestamp) {
                                processRemote = true;
                            } else if(localTimestamp < remoteTimestamp) {
                                processLocal = true;
                            }
                        }
                        if(processLocal) {
                            if(local !== undefined && local !== null) {
                                onupdatelocalvalue(key, remote, local);
                                onsavelocalvalue(key, remote, local);
                                onfinish(Sync.Mode.UPDATE_LOCAL, key, remote, local);
                            } else {
                                onaddlocalvalue(key, remote);
                                onsavelocalvalue(key, remote);
                                onfinish(Sync.Mode.ADD_LOCAL, key, remote);
                            }
                        } else if(processRemote) {
                            if(local.constructor === Object && !local[DATABASE.SYNCED]) {
                                local[DATABASE.SYNCED] = firebase.database.ServerValue.TIMESTAMP;
                            }
                            updates[key] = local;
                            delete local[DATABASE.KEYS];
                            self._ref.update(updates).then(function () {
                                self._getValue(options.key, function(key, updated) {
                                    if(remote !== undefined && remote !== null) {
                                        onupdateremotevalue(key, updated, remote);
                                        onsaveremotevalue(key, updated, remote);
                                        onfinish(Sync.Mode.UPDATE_REMOTE, key, updated, remote);
                                        registerHistory.call(self, Sync.Mode.UPDATE_REMOTE, self._ref.key + "/" + key, updated)
                                    } else {
                                        onaddremotevalue(key, updated);
                                        onsaveremotevalue(key, updated);
                                        onfinish(Sync.Mode.ADD_REMOTE, key, updated);
                                        registerHistory.call(self, Sync.Mode.ADD_REMOTE, self._ref.key + "/" + key, updated)
                                    }
                                }, null, onerror);
                            }).catch(onerror);
                        } else {
                            onfinish(Sync.Mode.SKIP, key);
                        }
                        break;
                    default:
                        onerror(key, "Mode not defined");
                        break;
                }
            }, onfinish, onerror);
        };

        function registerHistory(mode, key, value) {
            if(!options.log) return;
            var item = {};
            item[DATABASE.KEYS] = key;
            item[DATABASE.TIMESTAMP] = firebase.database.ServerValue.TIMESTAMP;
            item[DATABASE.MODE] = mode;
            if(value !== null && value !== undefined) {
                if(value.constructor === Boolean) {
                    item[DATABASE.VALUE] = value;
                } else if(value.constructor === Number) {
                    item[DATABASE.VALUE] = value;
                } else if(value.constructor === String) {
                    if(value.length < 50) {
                        item[DATABASE.VALUE] = value;
                    } else {
                        item[DATABASE.VALUE] = value.substr(0, 40) + "...";
                    }
                } else if(value.constructor === Object) {
                    item[DATABASE.VALUE] = "[" + value.constructor.name + "]";
                } else if(value.constructor === Array) {
                    item[DATABASE.VALUE] = "Array(" + value.length + ")";
                }
            }
            getRef().child(DATABASE.HISTORY).push().set(item);
        }

        this.syncValues = function(values) {
            var self = this;
            if(values && values.constructor !== Array) {
                options.onerror(options.key, "Values incorrect, set array as argument or use 'ongetvalue'.");
                return;
            }

            this._ref = getRef(options.child);
            if(!this._ref) return;

            var onfail = function(error){
                options.onerror(options.key, error);
                options.onfinish(options.mode, options.key);
            };

            var result = {};
            for(var i in values) {
                if(!values [i]) {
                    continue;
                } else if(values[i] && values[i].constructor !== Object) {
                    options.onerror(options.key, "Some of local values is not an object, use 'syncValue' for each one.");
                    options.onfinish(options.mode, options.key);
                    return;
                }
                //if(values[i][DATABASE.SYNCED]) values[i][DATABASE.SYNCED] = firebase.database.ServerValue.TIMESTAMP;

                var key = values[i][DATABASE.KEYS];
//                delete values[i][DATABASE.KEYS];
                if(!key) key = options.reference.push().key;
                result[key] = values[i];
            }

//a = new utils.sync({
//    type: utils.sync.Type.ACCOUNT_PRIVATE,
//    key: "test-2"
//});
//b=[];b = [{a:1,b:2},{a:2,b:3},{a:3,b:4,c:5}]
//{"-key1":{"a":1,"b":2},"-key2":{"a":2,"b":3},"-key3":{"a":3,"b":4,"c":5}}

//b = [{a: 11, b: 12, sy: 1506882099839, k:"-KvOA0Z_e39CoeRT6UnR"},
//    {a: 2, b: 3, sy: 1506882099978, k:"-KvOA0Z_e39CoeRT6UnS"},
//    {a: 3, b: 4, c: 5, sy: 1506882099979, k:"-KvOA0ZaGd5rZ2-7D-8L"}]

            this._ref.child(options.key).once("value").then(function(data) {
                var value = data.val() || {};
                if(value.constructor !== Object) {
                    options.onerror(options.key, "Remote value is not an object, use 'syncValue'.");
                    options.onfinish(options.mode, options.key);
                    return;
                }

                for(var x in value) {
                    if(!result[x]) result[x] = null;
                }

                var keys = u.keys(result);
                if(keys && keys.length > 0) {
                    var remoteUpdated = 0;
                    var localUpdated = 0;
                    var counter = 0;
                    var _onfinish = function(mode, key, newValue, oldValue) {
                        switch(mode) {
                            case Sync.Mode.UPDATE_REMOTE:
                            case Sync.Mode.OVERRIDE_REMOTE:
                            case Sync.Mode.ADD_REMOTE:
                            case Sync.Mode.REMOVE_REMOTE:
                                remoteUpdated ++;
                                break;
                            case Sync.Mode.UPDATE_LOCAL:
                            case Sync.Mode.OVERRIDE_LOCAL:
                            case Sync.Mode.REMOVE_LOCAL:
                            case Sync.Mode.ADD_LOCAL:
                                localUpdated ++;
                                break;
                            default:
                        }
                        onfinish(mode, key, newValue, oldValue);

                        counter ++;
                        if(remoteUpdated > 0 && counter === keys.length) {
                            u.save("synced:" + options.type, new Date().getTime());
                            updateTimestamp();
                        }
                    };

                    for(var i in keys) {
                        var sync = new Sync({
                            type: options.type,
                            child: options.key,
                            key: keys[i]
                        });
                        sync._ref = getRef(options.key);
                        //sync._syncValue(result[x]);
                        var onsaveLocal = function(key,newValue,oldValue) {
                            newValue[DATABASE.KEYS] = key;
                            if(result[key]) {
                                for(var x in result[key]) {
                                    delete result[key][x];
                                }
                                for(var x in newValue) {
                                    result[key][x] = newValue[x];
                                }
                                result[key][DATABASE.KEYS] = key;
                            } else {
                                values.push(newValue);
                            }
                            this(key,newValue,oldValue);
                        };
                        var onsaveRemote = function(key,newValue,oldValue) {
                            newValue[DATABASE.KEYS] = key;
                            if(result[key]) {
                                result[key][DATABASE.KEYS] = key;
                            } else {
                                values.push(newValue);
                            }
                            if(result[key]) result[key][DATABASE.SYNCED] = newValue[DATABASE.SYNCED];
                            this(key, newValue, oldValue);
                        };
                        sync._syncValue(Sync.Mode.UPDATE_BOTH, result[keys[i]], options.ongetvalue, options.onaddremotevalue, options.onupdateremotevalue, options.onremoveremotevalue, onsaveRemote.bind(options.onsaveremotevalue), options.onaddlocalvalue, options.onupdatelocalvalue, options.onremovelocalvalue, onsaveLocal.bind(options.onsavelocalvalue), _onfinish, options.onerror);
                    }
                }
            }).catch(onfail);
        };

        function onfinish(mode, key, newValue, oldValue) {
            if(options.debug && mode !== Sync.Mode.SKIP) console.warn(mode, key, "[new]:", newValue, "[old]:", oldValue);
            try {
                options.onfinish(mode, key, newValue);
            } catch(e) {
                options.onerror(key, e);
            }
        }

        this.syncValue = function(value) {
            this._ref = getRef(options.child);
            if(!this._ref) return;

            var _onfinish = function(mode, key, newValue, oldValue) {
                onfinish(mode, key, newValue, oldValue);
                u.save("synced:" + options.type, new Date().getTime());
            };

            this._syncValue(Sync.Mode.UPDATE_BOTH, value, options.ongetvalue, options.onaddremotevalue, options.onupdateremotevalue, options.onremoveremotevalue, onsaveremotevalueWithTimestamp, options.onaddlocalvalue, options.onupdatelocalvalue, options.onremovelocalvalue, options.onsavelocalvalue, _onfinish, options.onerror);
        };

        this.updateRemoteValue = function(value) {
            this._ref = getRef(options.child);
            if(!this._ref) return;
            this._syncValue(Sync.Mode.UPDATE_REMOTE, value, options.ongetvalue, options.onaddremotevalue, options.onupdateremotevalue, options.onremoveremotevalue, onsaveremotevalueWithTimestamp, options.onaddlocalvalue, options.onupdatelocalvalue, options.onremovelocalvalue, options.onsavelocalvalue, onfinish, options.onerror);
        };

        this.overrideRemoteValue = function(value) {
            this._ref = getRef(options.child);
            if(!this._ref) return;
            this._syncValue(Sync.Mode.OVERRIDE_REMOTE, value, options.ongetvalue, options.onaddremotevalue, options.onupdateremotevalue, options.onremoveremotevalue, onsaveremotevalueWithTimestamp, options.onaddlocalvalue, options.onupdatelocalvalue, options.onremovelocalvalue, options.onsavelocalvalue, onfinish, options.onerror);
        };

        this.overrideLocalValue = function(value) {
            this._ref = getRef(options.child);
            if(!this._ref) return;
            this._syncValue(Sync.Mode.OVERRIDE_LOCAL, value, options.ongetvalue, options.onaddremotevalue, options.onupdateremotevalue, options.onremoveremotevalue, options.onsaveremotevalue, options.onaddlocalvalue, options.onupdatelocalvalue, options.onremovelocalvalue, options.onsavelocalvalue, onfinish, options.onerror);
        };

        this.updateLocalValue = function(value) {
            this._ref = getRef(options.child);
            if(!this._ref) return;
            this._syncValue(Sync.Mode.UPDATE_LOCAL, value, options.ongetvalue, options.onaddremotevalue, options.onupdateremotevalue, options.onremoveremotevalue, options.onsaveremotevalue, options.onaddlocalvalue, options.onupdatelocalvalue, options.onremovelocalvalue, options.onsavelocalvalue, onfinish, options.onerror);
        };

        function getRef(child) {
            var ref;
            switch(options.type) {
                case Sync.Type.ACCOUNT_PRIVATE:
                    if(!options.uid) {
                        options.onerror(options.key, "UID not defined.");
                        return;
                    } else {
                        ref = options.reference.child(DATABASE.SECTION_USERS).child(options.uid).child(DATABASE.PRIVATE);
                    }
                    break;
                case Sync.Type.USER_PUBLIC:
                    if(!options.group) {
                        options.onerror(options.key, "Group not defined.");
                        return;
                    }
                    if(!options.userNumber) {
                        options.onerror(options.key, "UserNumber not defined.");
                        return;
                    }
                    ref = options.reference.child(options.group).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(options.userNumber);
                    break;
            }
            if(!ref) {
                options.onerror(options.key, "Firebase database reference not defined.");
                return;
            }
            if(child) ref = ref.child(child);
            return ref;
        }

        this.ready = function() {
            this._ref = getRef();
            if(!this._ref) return false;
            return !(!firebase || !firebase.auth() || !firebase.auth().currentUser || !firebase.auth().currentUser.uid);

        };

        function onsaveremotevalueWithTimestamp(key, newValue, oldValue) {
            options.onsaveremotevalue(key, newValue, oldValue);
            updateTimestamp();
        }

        function updateTimestamp() {
            var ref;
            switch(options.type) {
                case Sync.Type.ACCOUNT_PRIVATE:
                    ref = options.reference.child(DATABASE.SECTION_USERS).child(options.uid).child(DATABASE.PRIVATE);
                    break;
                case Sync.Type.USER_PUBLIC:
                    ref = options.reference.child(options.group).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(options.userNumber);
                    break;
            }
            var update = {};
            update[DATABASE.SYNCED] = firebase.database.ServerValue.TIMESTAMP;

            options._delayedWatch = options._delayedWatch || {};
            options._delayedWatch[ref.toString()] = options._delayedWatch[ref.toString()] || {};
            if(Sync._specialWatch) {
                for(var x in Sync._specialWatch) {
                    Sync._specialWatch[x].ref.off();
                    options._delayedWatch[ref.toString()][x] = Sync._specialWatch[x];
                }
                Sync._specialWatch = {};
            }

            ref.update(update).then(function(data){
                ref.child(DATABASE.SYNCED).once("value").then(function(data) {
                    if(options._delayedWatch && options._delayedWatch[ref.toString()]) {
                        for(var x in options._delayedWatch[ref.toString()]) {
                            Sync._specialWatch[x] = options._delayedWatch[ref.toString()][x];
                            u.save("synced:"+options.type, data.val());
                            Sync._specialWatch[x].ref.on("value", function(data) {
                                console.log("WATCH", x, u.load("synced:"+options.type), data.val());
                                if(u.load("synced:"+options.type) < data.val()) {
                                    Sync._watch[x] = Sync._specialWatch[x];
                                    Sync._specialWatch[x].callback(Sync._specialWatch[x].ref.toString(), data.val());
                                }
                            }, function(error) {
                                options.error(Sync._specialWatch[x].ref.toString(), error);
                            })
                        }
                        options._delayedWatch[ref.toString()] = {};
                    }
                }).catch(function(error){
                    options.error(ref.toString(), error);
                })
            });
        }

        this.watch = function(key, onchangevalue) {
            if(key.constructor === Function) {
                onchangevalue = key;
                key = null;
            }
            Sync._watch = Sync._watch || {};

            var path = [];
            if(options.key) path.push(options.key);
            if(key) path.push(key);
            if(path.length > 0) path.join("/");

            this._ref = getRef(path.length > 0 ? path.join("/") : undefined);
            if(!this._ref) return false;

            var watched = this._ref.toString();
            if(options.key === DATABASE.SYNCED) {
                options.onerror(DATABASE.SYNCED + " cannot be watched directly, use 'watchChanges' instead.");
            } else if(onchangevalue && Sync._watch[watched]) {
                console.warn(options.key + " already watching.");
            } else if(!onchangevalue && Sync._watch[watched]) {
                Sync._watch[watched].ref.off();
                delete Sync._watch[watched];
            } else if(onchangevalue && !Sync._watch[watched]) {
                this._ref.on("value", function(data) {
                    onchangevalue(data.key, data.val());
                    Sync._watch[watched] = {
                        ref: data.ref,
                        callback: onchangevalue
                    }
                }, function(error) {
                    options.error(data.key, error);
                })
            } else if(!onchangevalue && !Sync._watch[watched]) {
                console.warn(watched + " is not watching, define 'onchangevalue'.")
            }
        };

        this.watchChanges = function(callback) {
            Sync._watch = Sync._watch || {};
            var ref = getRef(DATABASE.SYNCED);
            var watched = ref.toString();
            Sync._specialWatch = Sync._specialWatch || {};
            if(!callback && Sync._specialWatch[watched]) {
                Sync._specialWatch[watched].ref.off();
                delete Sync._watch[watched];
                delete Sync._specialWatch[watched];
            } else if(callback) {
                ref.on("value", function(data) {
                    callback(watched, data.val());
                    Sync._specialWatch[watched] = Sync._watch[watched] = {
                        ref: data.ref,
                        callback: callback,
                        timestamp: data.val()
                    };
                }, function(error) {
                    options.error(watched, error);
                })
            } else if(!callback && !Sync._specialWatch[watched]) {
                console.warn(options.key + " is not watching yet.")
            }
        };

    }
    Sync.Type = {
        ACCOUNT_PRIVATE: "account-private",
        USER_PUBLIC: "user-public"
    };
    Sync.Mode = {
        ADD_REMOTE: "ra",
        UPDATE_REMOTE: "ru",
        OVERRIDE_REMOTE: "ro",
        REMOVE_REMOTE: "rr",
        ADD_LOCAL: "la",
        UPDATE_LOCAL: "lu",
        OVERRIDE_LOCAL: "lo",
        REMOVE_LOCAL: "lr",
        UPDATE_BOTH: "bu",
        SKIP: "sk",
        GET_REMOTE: "rg"
    };
    Sync.CREATE_KEY = "$create_key$";


    function isActiveTime(timestamp) {
        return (new Date().getTime() - timestamp) / 1000 <= 3600;
    }

    function isEnabledTime(timestamp) {
        return (new Date().getTime() - timestamp) / 1000 <= 120;
    }

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
        isActiveTime:isActiveTime,
        isEnabledTime:isEnabledTime
    }
}
