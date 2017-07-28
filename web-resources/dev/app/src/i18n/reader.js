import _ from 'lodash';

class i18nLoader {

    loadProperties(url, defaultLangURL, successCallback, errorCallback) {

        let propertyFileParser = (data) => {
            let stripLine = (line) => {
                let result = line;
                if (_.trim(line).indexOf('#') === 0) {
                    result = line.substr(0, line.indexOf('#'));
                }
                return _.trim(result);
            };

            let splitKeyValue = (line) => {
                let result = {};
                let index = line.indexOf('=');
                if (index >= 0) {
                    result['key'] = _.trim(line.substr(0, index));
                    result['value'] = _.trim(line.substr(index + 1));
                    return result;
                }
                return null;
            };

            let parsed = {};
            let lines = data.split(/\r\n|\n|\r/g);
            for (let key in lines) {
                let result = splitKeyValue(stripLine(lines[key]));
                if (result) {
                    parsed[result.key] = result.value;
                }
            }
            return parsed;
        };

        let parseData = (defaultData, localizationData) => {
            let parsedDefault, parsedLocalization;
            if (!defaultData) {
                if (!localizationData) {
                    errorCallback.call(this);
                } else {
                    parsedLocalization = propertyFileParser(localizationData);
                    successCallback.call(this, parsedLocalization);
                }
            } else {
                if (!localizationData) {
                    parsedDefault = propertyFileParser(defaultData);
                    successCallback.call(this, parsedDefault);
                } else {
                    parsedLocalization = propertyFileParser(localizationData);
                    parsedDefault = propertyFileParser(defaultData);
                    for (let key in parsedDefault) {
                        if (parsedLocalization[key] && parsedLocalization[key] != "") {
                            parsedDefault[key] = parsedLocalization[key];
                        }
                    }
                    successCallback.call(this, parsedDefault);
                }
            }
        };

        if (defaultLangURL == url)
            this.getResourse(defaultLangURL).done((defaultData) => {
                parseData(defaultData, null);
            }).fail(() => {
                parseData(null, null);
            });
        else
            this.getResourse(defaultLangURL).done((defaultData) => {
                this.getResourse(url).done((localizationData) => {
                    parseData(defaultData, localizationData);
                }).fail(() => {
                    parseData(defaultData, null);
                })
            }).fail(() => {
                this.getResourse(url).done((localizationData) => {
                    parseData(null, localizationData);
                }).fail(() => {
                    parseData(null, null);
                })
            });

    }

    getResourse(url) {
        return $.ajax({
            type: 'get',
            url: url,
            dataType: 'text',
        });
    }
}

export default new i18nLoader()