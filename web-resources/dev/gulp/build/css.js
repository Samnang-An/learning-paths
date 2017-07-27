import gulp from 'gulp';
import sass from 'gulp-sass';
import plumber from 'gulp-plumber';
import config from '../config';

gulp.task('css', () => {
  return gulp.src([
      `${config.resourcesDir}/css/*.scss`
    ], { base: config.resourcesDir })
    .pipe(sass({ outputStyle: 'expanded' }))
    .pipe(gulp.dest(config.resourcesDir));
});
