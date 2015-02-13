'use strict';

var browserify = require('browserify');
var gulp = require('gulp');
var transform = require('vinyl-transform');
var source = require('vinyl-source-stream');
var uglify = require('gulp-uglify');
var sourcemaps = require('gulp-sourcemaps');
var rename = require('gulp-rename');
var reactify = require('reactify');
var watchify = require('watchify');
var webserver = require('gulp-webserver');
var buffer = require('vinyl-buffer');


gulp.task('webserver', function() {
    gulp.src('app')
    .pipe(webserver({
        livereload: true,
        directoryListing: true,
        open: true
    }));
});

var getBundler = function () {
    
    return browserify({
    entries: ['./js/app.js'], // Only need initial file, browserify finds the deps
    transform: [reactify], // We want to convert JSX to normal javascript
    debug: true, // Gives us sourcemapping
    cache: {}, packageCache: {}, fullPaths: true // Requirement of watchify
})};

gulp.task('javascript', function () {

  var b = getBundler();
  
  return b.bundle()
    .pipe(source('./js/app.js'))
    .pipe(buffer())
    //.pipe(uglify())
    .pipe(rename('myriad.js'))
    .pipe(gulp.dest('./public/js'));
});

gulp.task('html', function () {
    return gulp.src('*.html')
        .pipe( gulp.dest('public/'))
});

gulp.task('css', function () {
    return gulp.src('css/*.css')
        .pipe( gulp.dest('public/css/'))
});

gulp.task('img', function () {
    return gulp.src('img/**')
        .pipe( gulp.dest('public/img/'))
});

gulp.task('dev-js', function () {
 
    var watcher  = watchify(getBundler());

    return watcher
    .on('update', function () { // When any files update
        var updateStart = Date.now();
        console.log('Updating!');
        watcher.bundle() // Create new bundle that uses the cache for high performance
        .pipe(source('app.js'))
    // This is where you add uglifying etc.
        .pipe(gulp.dest('./public/js/'));
        console.log('Updated!', (Date.now() - updateStart) + 'ms');
    })
    .bundle() // Create the initial bundle when starting the task
    .pipe(source('app.js'))
    .pipe(rename('myriad.js'))
    .pipe(gulp.dest('./public/js/'));

});

gulp.task('webserver', function() {
  gulp.src('./public')
    .pipe(webserver({
      livereload: true,
      directoryListing: false,
      open: true,
      port: 8888
    }));
});

gulp.task('watch', function() {
    gulp.watch('index.html', ['html']);
    gulp.watch('css/**', ['css']);
    gulp.watch('js/**', ['javascript']);
    gulp.watch('img/**', ['img']);
});

gulp.task('dev', ['default', 'watch', 'webserver']);

gulp.task('default', ['javascript', 'html', 'css', 'img']);

