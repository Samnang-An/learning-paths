import gulp from 'gulp';
import webpack from 'webpack-stream';
import config from '../config';
import notify from 'gulp-notify';
import plumber from 'gulp-plumber';
import webpackConfig from "../../config/webpack.config.js";
import * as extensions from "../../config/webpack-ext.config";

let wConfig = Object.create(webpackConfig);

if (config.liferayVersion && config.liferayVersion == 7) {
    // merge object in output
    Object.assign(extensions.amdConfig.output, wConfig.output);
    Object.assign(wConfig, extensions.amdConfig);
}
else {
    Object.assign(wConfig, extensions.defConfig);
}

gulp.task('webpack', function() {
    return gulp.src(`${config.appDir}/src/` + wConfig.entry)
        .pipe(plumber())
        .pipe(webpack( wConfig ))
        .pipe(gulp.dest(`${config.distDir}/`))
        .pipe(notify("Build completed"));
});