import gulp from 'gulp';
import runSequence from 'run-sequence';
import config from '../config';

gulp.task('build', (callback) => {

    const sequence = [
        'css',
        [
            'copy-index',
            'copyValamisJStoDist',
            'copyResourcesToDist'
        ],
        //'copyValamisJS',
        'webpack'
    ];

    if(config.yuiCompress) {
        sequence.push('replace-js');
        sequence.push('yui-compressor');

    }

    sequence.push('copyApplication');  // put to yui compress block

    sequence.push(callback);

    runSequence.apply(this, sequence);
});
