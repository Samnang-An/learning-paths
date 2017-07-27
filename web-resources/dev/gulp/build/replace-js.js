import gulp from "gulp";
import replace from "gulp-replace-task";

gulp.task('replace-js', () => {
    //const patterns = [];

    const patterns = [{
        match: /final\b/g,
        replacement: 'finalt'
    },
        {
            match: /1..toPrecision/g,
            replacement: '(1.0).toPrecision'
        },
        {
            match: /1..toFixed/,
            replacement: '(1.0).toFixed'
        },
        {
            match: /delegate.iterator.return/,
            replacement: 'delegate.iterator["return"]'
        }
    ];

    return gulp.src('dist/application.js')
        .pipe(replace({patterns}))
        .pipe(gulp.dest('dist/'));
});