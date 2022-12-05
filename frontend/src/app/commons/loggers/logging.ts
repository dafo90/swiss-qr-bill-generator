import { environment } from '../../../environments/environment';

const logConfig = {
  debug: {
    levels: ['debug'],
    log: (...params: any[]) => console.debug(...params)
  },
  info: {
    levels:['debug', 'info'],
    log: (...params: any[]) => console.log(...params)
  },
  error: {
    levels:['debug', 'info', 'error'],
    log: (...params: any[]) => console.error(...params)
  }
}

const log = (logLevel:'debug'|'info'|'error', ...params: any[]) => {
  if (logConfig[logLevel].levels.includes(environment.logLevel)) logConfig[logLevel].log(...params);
}

export function logDebug(...params: any[]) {
  log("debug", params);
}

export function logInfo(...params: any[]) {
  log("info", params);
}

export function logError(...params: any[]) {
  log("error", params);
}
