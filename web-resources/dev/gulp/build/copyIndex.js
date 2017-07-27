import gulp from 'gulp';
import plumber from 'gulp-plumber';
import config from '../config';
import rename from 'gulp-rename';


let indexPath = `${config.appDir}/` + (config.liferayVersion == 7? 'index.html': 'index_lf6.html');

gulp.task('copy-index', () => {
  return gulp.src([indexPath], {base: config.appDir})
    .pipe(plumber())
    .pipe(rename('index.html'))
    .pipe(gulp.dest(config.distDir));
});