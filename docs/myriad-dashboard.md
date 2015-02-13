# Myriad Webapp

The Myriad webapp is a [React](http://facebook.github.io/react/) single page application served by Jetty embedded in the myriad jar.

## Building

The app uses [NPM](https://www.npmjs.com/) to manage depencies and [Gulp](http://gulpjs.com/) to assemble the distribution. 
The app is served from the webapp/public directory. 
To get setup, install `npm` and `gulp` and from the webapp directory execute

```
npm install
gulp
```

## Developing

The gulpfile contains a dev target that launches a node.js webserver and watches the webapp files, re-assembling when
files change. To launch simply run

```
gulp dev
```

A browser window should open with the site loaded. If not, it uses [port 8888](http://localhost:8888) 
It is helpful to have myriad setup in Vagrant locally so the api is available. Default values are coded into
the dashboard if Myriad api isn't available.

