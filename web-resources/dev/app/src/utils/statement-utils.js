
import LiferayUtil from 'utils/liferay-utils';

class StatementUtil {
    static getLangDictionaryTincanValue(value, lang) {
        let langDict = value,
            key;

        if (typeof lang === 'undefined') {
            lang = LiferayUtil.getUserLocale().replace('_', '-');
        }

        if (typeof lang !== 'undefined' && typeof langDict[lang] !== 'undefined') {
            return langDict[lang];
        }
        if (typeof langDict.und !== 'undefined') {
            return langDict.und;
        }
        if (typeof langDict['en-US'] !== 'undefined') {
            return langDict['en-US'];
        }

        for (key in langDict) {
            if (langDict.hasOwnProperty(key)) {
                return langDict[key];
            }
        }

        return '';
    }
    static getVerbPostfix(verb) {
        let arr = verb.split('/');
        return arr[arr.length - 1];
    }
}

export default StatementUtil;
