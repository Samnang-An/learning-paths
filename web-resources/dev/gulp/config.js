import dotenv from 'dotenv';
import configParser from './modules/config_parser';

dotenv.load();

const gulpConfig = {
    env: process.env.NODE_ENV,
    mode: process.env.MODE,
    appDir: 'app',
    resourcesDir: './../../web-resources/src/main/resources',
    warDir: './../../liferay6-war-bundle/src/main/webapp/',
    bundleDir: './../../liferay7-jar-bundle/src/main/resources/META-INF/resources/',
    distDir: 'dist',
    testDir: 'specs',
    mocksDir: 'mocks',
    configDir: 'config',
    gulpDir: 'gulp',
    liferayVersion: 7,
    yuiCompress: true,  // must be true for compiling application for Liferay
    [process.env.NODE_ENV]: true
};
const config = configParser(gulpConfig.env);

export default Object.assign(gulpConfig, config);
