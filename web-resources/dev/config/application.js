export default {
    apiPath: '@@apiPath',
    resourcePath: '@@resourcePath',
    langPath: (lang) => {
        return '@@resourcePath' + 'i18n/' + 'lessonStudio_' + lang + '.properties';
    }
};