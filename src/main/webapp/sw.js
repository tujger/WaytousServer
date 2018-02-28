/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 3/9/17.
 */
var CACHE_NAME = "${APP_NAME}-v${SERVER_BUILD}";
var urlsToCache = [
    "/group/",
    "/css/tracking.css",
    "/css/edequate.css",
    "/images/edeqa-logo.svg",
    "/images/logo.svg",
    "/images/marker.svg",
    "/images/navigation_outline.svg",
    "/images/navigation_twinks.svg",
    "/js/helpers/Constants.js",
    "/js/Edequate.js",
    "/js/helpers/MyUser.js",
    "/js/helpers/MyUsers.js",
    "/js/helpers/TrackingFB.js",
    "/js/helpers/Utils.js",
    "/js/tracking/Main.js",
    "/js/all.js",
    "/resources/en/tracking.json",
    "/icons/android-chrome-192x192.png",
    "/icons/favicon-32x32.png",
    "/icons/favicon-16x16.png",
    "/icons/favicon-194x194.png",
    "/icons/favicon.ico",
    "/sounds/office-1.mp3",
    "/sounds/oringz-w426.mp3",
    "/sounds/youve-been-informed.mp3",
    "/sounds/oringz-w427.mp3"
];

self.addEventListener("install", function(event) {
    event.waitUntil(
        caches.open(CACHE_NAME).then(function(cache) {
            console.log("Opened cache");
            return cache.addAll(urlsToCache);
        }).then(function() {
            // `skipWaiting()` forces the waiting ServiceWorker to become the
            // active ServiceWorker, triggering the `onactivate` event.
            // Together with `Clients.claim()` this allows a worker to take effect
            // immediately in the client(s).
            return self.skipWaiting();
        })
    );
});

self.addEventListener("fetch", function(event) {

    if (event.request.method !== 'GET') {
        /* If we don't block the event as shown below, then the request will go to
           the network as usual.
        */
//        console.log('WORKER: fetch event ignored.', event.request.method, event.request.url);
        return;
    }
    event.respondWith(
        caches.match(event.request)
            .then(function(response) {
                if (response) {
//            console.log("Return cached",event.request);
                    return response;
                }

                var fetchRequest = event.request.clone();

                return fetch(fetchRequest).then(
                    function(response) {
                        if(!response || response.status !== 200 || response.type !== "basic") {
//                console.log("Return cached",response);
                            return response;
                        }

                        var responseToCache = response.clone();

                        caches.open(CACHE_NAME).then(function(cache) {
                            cache.put(event.request, responseToCache);
                        });
//            console.log("Return cached",response);
                        return response;
                    }
                );
            })
    );
});

self.addEventListener("activate", function(event) {
    var cacheWhitelist = [CACHE_NAME];
    event.waitUntil(
        caches.keys().then(function(cacheNames) {
            return Promise.all(
                cacheNames.map(function(cacheName) {
                    if (cacheWhitelist.indexOf(cacheName) === -1) {
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
    return self.clients.claim();
});