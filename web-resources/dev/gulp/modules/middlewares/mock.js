import prism from 'connect-prism';
import historyApiFallback from 'connect-history-api-fallback';
import config from '../../config';

export default () => {
    if (config.mode === 'mock') {
        prism.create({
            name: 'serve',
            mode: 'mock',
            context: config.mock.path,
            host: config.mock.host,
            port: config.mock.port,
            delay: 0,
            rewrite: {},
            mockFilenameGenerator(config, req) {
                var getParam = function (url, name) {
                    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(url);
                    if (!results) {
                        return undefined;
                    }
                    return results[1] || undefined;
                };
                var url = req._parsedUrl.search;

                var page = getParam(url, 'page');
                var itemsPerPage = getParam(url, 'itemsPerPage');

                if(page) {
                    return `${req._parsedUrl.pathname.replace(/^\//, '')}_${req.method}_${page}.json`;
                }
                return `${req._parsedUrl.pathname.replace(/^\//, '')}_${req.method}.json`;
            }
        });

        return [
            prism.middleware,
            historyApiFallback()
        ];
    }
};
