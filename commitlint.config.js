module.exports = {
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'chore', 'docs', 'style', 'refactor', 'perf', 'test', 'build', 'ci', 'revert'],
    ],
    'type-empty': [2, 'never'],
    // Disable all formatting/style rules
    'type-case': [0],
    'subject-case': [0],
    'subject-empty': [0],
    'subject-full-stop': [0],
    'header-max-length': [0],
    'body-max-line-length': [0],
    'footer-max-line-length': [0],
    'body-leading-blank': [0],
    'footer-leading-blank': [0],
  },
};
