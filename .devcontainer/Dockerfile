ARG VARIANT=0.205.5-17-jdk-bullseye
FROM mcr.microsoft.com/vscode/devcontainers/java:${VARIANT}

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# Set locale
RUN apt-get install -y locales \
  && sed -i '/^#.* ja_JP.UTF-8 /s/^#//' /etc/locale.gen \
  && locale-gen \
  && ln -fs /usr/share/zoneinfo/Asia/Tokyo /etc/localtime\
  && dpkg-reconfigure -f noninteractive tzdata
ENV LANG="ja_JP.UTF-8"
ENV LANGUAGE="ja_JP:ja"
ENV LC_ALL="ja_JP.UTF-8"
