'use strict';

var gulp = require('gulp');

var transform = require('vinyl-transform');
var source = require('vinyl-source-stream');
var buffer = require('vinyl-buffer');
var watchify = require('watchify');
var babelify = require('babelify');
var browserify = require('browserify');
var rename = require('gulp-rename');
var uglify = require('gulp-uglify');
var webserver = require('gulp-webserver');
var del = require('del');

gulp.task("js", ['clean'], function () {
  browserify({
    entries: ['./js/app.js'], // Only need initial file, browserify finds the deps
    transform: ['babelify'],
    debug: false,
    fullPaths: false
    })
    .bundle()
    .pipe(source('bundle.js'))
    .pipe(rename('myriad.js'))
    .pipe(buffer())
    .pipe(uglify())
    .pipe(gulp.dest("public/js/"));
});

gulp.task("js-dev", ['clean'], function () {
  browserify({
    entries: ['./js/app.js'], // Only need initial file, browserify finds the deps
    transform: ['babelify'],
    debug: true,
    fullPaths: true
    })
    .bundle()
    .pipe(source('bundle.js'))
    .pipe(rename('myriad.js'))
    .pipe(gulp.dest("public/js/"));
});


gulp.task('html', ['clean'], function () {
    return gulp.src('*.html')
        .pipe( gulp.dest('public/'))
});

gulp.task('css', ['clean'], function () {
    return gulp.src('css/*.css')
        .pipe( gulp.dest('public/css/'))
});

gulp.task('img', ['clean'], function () {
    return gulp.src('img/**')
        .pipe( gulp.dest('public/img/'))
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
    gulp.watch('js/**', ['js']);
    gulp.watch('img/**', ['img']);
});

gulp.task('clean', function() {
  return del(['./public']);
});

gulp.task('dev', ['js-dev', 'html', 'css', 'img', 'watch', 'webserver']);

gulp.task('default', ['js', 'html', 'css', 'img']);

gulp.task('build', ['default']); // gradle calls gulp build by default
