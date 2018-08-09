"use strict";

const pathModule = require('path')

const replacePathVariables = (path, data) => {
  const REGEXP_CAMEL_CASE_NAME = /\[camel-case-name\]/gi;
	if (typeof path === "function") {
		path = path(data);
	}

  if (data && data.chunk && data.chunk.name) {
    let tokens = data.chunk.name.split(pathModule.sep);
    return path.replace(
      REGEXP_CAMEL_CASE_NAME,
      tokens[tokens.length - 1]
        .replace(/(\-\w)/g, (matches) => { return matches[1].toUpperCase(); })
        .replace(/(^\w)/, (matches) => { return matches[0].toUpperCase(); })
    );
  } else {
    return path;
  }
};

const writeStats = (compilation) => {
  const ms = [];
  for (let module of compilation.getStats().toJson().modules) {
    let reasons = [];
    for (let reason of module.reasons) {
      reasons.push(reason.moduleName);
    }
    ms.push({
      name: module.name,
      reasons: reasons
    })
  }

  const s = JSON.stringify(ms);
  compilation.assets['sbt-vuefy-tree.json'] = {
    source() {
      return s;
    },
    size() {
      return s.length;
    }
  };
};

class SbtVuefyPlugin {
  apply(compiler) {
    compiler.plugin("compilation", (compilation) => {
      compilation.mainTemplate.plugin('asset-path', replacePathVariables);
    });

    compiler.plugin("emit", (compilation, callback) => {
      writeStats(compilation);
      callback();
    });
  }
}

module.exports = SbtVuefyPlugin;