import webpack from 'webpack';
import path from 'path';

let config = {
    cache: true,
    resolve: {
        root: [
            path.resolve('./app/src')
        ],
        alias: {
            "underscore": "lodash"
        },
        // tells webpack to query these directories for modules
        modulesDirectories: ['vendor', 'node_modules', 'valamis']
    },
    output: {
        //path: './dist',
        filename: 'application.js'
    },
    devtool: 'source-map',
    module: {
        loaders: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                loader: 'babel',
                query: {
                    //cacheDirectory: true,
                    plugins: ['transform-decorators-legacy'],
                    presets: ['es2015']
                }
            },
            {
                test: /\.html$/,
                loader: 'mustache'
                // loader: 'mustache?minify'
                // loader: 'mustache?{ minify: { removeComments: false } }'
                // loader: 'mustache?noShortcut'
            },
            {
                test: /\.scss$/,
                loaders: ["style-loader", "css-loader", "sass-loader"]
            }
        ]
    },
    plugins: [
        new webpack.ProvidePlugin(
            {
                $: "jquery",
                jQuery: "jquery",
                Backbone: 'backbone',
                Marionette: 'backbone.marionette'
            }
        ),
        new webpack.optimize.UglifyJsPlugin({
            minimize: false,
            mangle: false,
            compress: {
                warnings: false,
                properties: false
            },
            beautify: {
                keep_quoted_props: true,
                quote_keys: true
            },
            output: {
                comments: false,
                semicolons: true,
                quote_keys: true,
                keep_quoted_props: true,
                beautify: true
            }
        })
    ]
};

module.exports = config;