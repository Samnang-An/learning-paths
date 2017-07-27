import gulp from 'gulp';
import plumber from 'gulp-plumber';
import config from '../config';

gulp.task('copyValamisJStoDist', () => {
    return gulp.src([
        `${config.appDir}/valamis/**/*`
    ], {base: config.appDir})
        .pipe(plumber())
        .pipe(gulp.dest(`${config.distDir}/js`));
});

gulp.task('copyResourcesToDist', () => {
    return gulp.src([
          `${config.resourcesDir}/css/*`,
          `${config.resourcesDir}/img/**/*`,
          `${config.resourcesDir}/fonts/**/*`,
          `${config.resourcesDir}/i18n/*`
      ], { base: config.resourcesDir })
      .pipe(plumber())
      .pipe(gulp.dest(config.distDir));
});

//gulp.task('copyValamisJS', () => {
//    return gulp.src([
//            `${config.appDir}/valamis/**/*`, `!${config.appDir}/valamis/**/amd-loader.js`
//        ], {base: config.appDir})
//        .pipe(plumber())
//        .pipe(gulp.dest(`${config.resourcesDir}/js`));
//});

gulp.task('copyApplication', () => {
   let dest = config.liferayVersion == 7 ? `${config.bundleDir}/` : `${config.warDir}/`;

   return gulp.src([
           `${config.distDir}/*.js`
       ], {base: config.distDir})
       .pipe(plumber())
       .pipe(gulp.dest(dest));
});