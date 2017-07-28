import gulp from 'gulp';
import compress from 'gulp-yuicompressor';

gulp.task('yui-compressor', () => {
    return gulp.src('dist/application.js')
        .pipe(compress({
            type: 'js'
        }))
        .pipe(gulp.dest('dist/'));
});