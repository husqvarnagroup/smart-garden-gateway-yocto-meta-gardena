# Autogenerated with 'bitbake -c update_crates gateway-config-backend'

# from Cargo.lock
SRC_URI += " \
    crate://crates.io/actix/0.7.9 \
    crate://crates.io/actix-net/0.2.5 \
    crate://crates.io/actix-web/0.7.16 \
    crate://crates.io/actix_derive/0.3.2 \
    crate://crates.io/addr2line/0.17.0 \
    crate://crates.io/adler/1.0.2 \
    crate://crates.io/aho-corasick/0.7.19 \
    crate://crates.io/ansi_term/0.12.1 \
    crate://crates.io/askama_escape/0.1.0 \
    crate://crates.io/atty/0.2.14 \
    crate://crates.io/autocfg/0.1.8 \
    crate://crates.io/autocfg/1.1.0 \
    crate://crates.io/backtrace/0.3.66 \
    crate://crates.io/base64/0.10.1 \
    crate://crates.io/bindgen/0.57.0 \
    crate://crates.io/bitflags/1.3.2 \
    crate://crates.io/byteorder/1.4.3 \
    crate://crates.io/bytes/0.4.12 \
    crate://crates.io/cc/1.0.74 \
    crate://crates.io/cexpr/0.4.0 \
    crate://crates.io/cfg-if/0.1.10 \
    crate://crates.io/cfg-if/1.0.0 \
    crate://crates.io/clang-sys/1.4.0 \
    crate://crates.io/clap/2.34.0 \
    crate://crates.io/cloudabi/0.0.3 \
    crate://crates.io/cookie/0.11.5 \
    crate://crates.io/crossbeam-channel/0.3.9 \
    crate://crates.io/crossbeam-deque/0.7.4 \
    crate://crates.io/crossbeam-epoch/0.8.2 \
    crate://crates.io/crossbeam-queue/0.1.2 \
    crate://crates.io/crossbeam-queue/0.2.3 \
    crate://crates.io/crossbeam-utils/0.6.6 \
    crate://crates.io/crossbeam-utils/0.7.2 \
    crate://crates.io/dtoa/0.4.8 \
    crate://crates.io/encoding/0.2.33 \
    crate://crates.io/encoding-index-japanese/1.20141219.5 \
    crate://crates.io/encoding-index-korean/1.20141219.5 \
    crate://crates.io/encoding-index-simpchinese/1.20141219.5 \
    crate://crates.io/encoding-index-singlebyte/1.20141219.5 \
    crate://crates.io/encoding-index-tradchinese/1.20141219.5 \
    crate://crates.io/encoding_index_tests/0.1.4 \
    crate://crates.io/env_logger/0.6.2 \
    crate://crates.io/env_logger/0.8.4 \
    crate://crates.io/error-chain/0.8.1 \
    crate://crates.io/failure/0.1.8 \
    crate://crates.io/failure_derive/0.1.8 \
    crate://crates.io/fnv/1.0.7 \
    crate://crates.io/foreign-types/0.3.2 \
    crate://crates.io/foreign-types-shared/0.1.1 \
    crate://crates.io/fuchsia-cprng/0.1.1 \
    crate://crates.io/fuchsia-zircon/0.3.3 \
    crate://crates.io/fuchsia-zircon-sys/0.3.3 \
    crate://crates.io/futures/0.1.31 \
    crate://crates.io/futures/0.3.25 \
    crate://crates.io/futures-channel/0.3.25 \
    crate://crates.io/futures-core/0.3.25 \
    crate://crates.io/futures-cpupool/0.1.8 \
    crate://crates.io/futures-executor/0.3.25 \
    crate://crates.io/futures-io/0.3.25 \
    crate://crates.io/futures-macro/0.3.25 \
    crate://crates.io/futures-sink/0.3.25 \
    crate://crates.io/futures-task/0.3.25 \
    crate://crates.io/futures-util/0.3.25 \
    crate://crates.io/gimli/0.26.2 \
    crate://crates.io/glob/0.3.0 \
    crate://crates.io/h2/0.1.26 \
    crate://crates.io/hashbrown/0.12.3 \
    crate://crates.io/hermit-abi/0.1.19 \
    crate://crates.io/hostname/0.3.1 \
    crate://crates.io/http/0.1.21 \
    crate://crates.io/httparse/1.8.0 \
    crate://crates.io/humantime/1.3.0 \
    crate://crates.io/humantime/2.1.0 \
    crate://crates.io/idna/0.1.5 \
    crate://crates.io/indexmap/1.9.1 \
    crate://crates.io/iovec/0.1.4 \
    crate://crates.io/ipconfig/0.1.9 \
    crate://crates.io/itoa/0.4.8 \
    crate://crates.io/itoa/1.0.4 \
    crate://crates.io/kernel32-sys/0.2.2 \
    crate://crates.io/language-tags/0.2.2 \
    crate://crates.io/lazy_static/1.4.0 \
    crate://crates.io/lazycell/1.3.0 \
    crate://crates.io/libc/0.2.137 \
    crate://crates.io/libloading/0.7.3 \
    crate://crates.io/linked-hash-map/0.5.6 \
    crate://crates.io/listenfd/0.3.5 \
    crate://crates.io/lock_api/0.1.5 \
    crate://crates.io/lock_api/0.3.4 \
    crate://crates.io/log/0.4.17 \
    crate://crates.io/lru-cache/0.1.2 \
    crate://crates.io/match_cfg/0.1.0 \
    crate://crates.io/matches/0.1.9 \
    crate://crates.io/maybe-uninit/2.0.0 \
    crate://crates.io/memchr/2.5.0 \
    crate://crates.io/memoffset/0.5.6 \
    crate://crates.io/mime/0.3.16 \
    crate://crates.io/mime_guess/2.0.4 \
    crate://crates.io/miniz_oxide/0.5.4 \
    crate://crates.io/mio/0.6.23 \
    crate://crates.io/mio-named-pipes/0.1.7 \
    crate://crates.io/mio-uds/0.6.8 \
    crate://crates.io/miow/0.2.2 \
    crate://crates.io/miow/0.3.7 \
    crate://crates.io/net2/0.2.38 \
    crate://crates.io/nom/5.1.2 \
    crate://crates.io/num_cpus/1.13.1 \
    crate://crates.io/object/0.29.0 \
    crate://crates.io/once_cell/1.16.0 \
    crate://crates.io/openssl/0.10.42 \
    crate://crates.io/openssl-macros/0.1.0 \
    crate://crates.io/openssl-sys/0.9.77 \
    crate://crates.io/owning_ref/0.4.1 \
    crate://crates.io/parking_lot/0.7.1 \
    crate://crates.io/parking_lot/0.9.0 \
    crate://crates.io/parking_lot_core/0.4.0 \
    crate://crates.io/parking_lot_core/0.6.2 \
    crate://crates.io/peeking_take_while/0.1.2 \
    crate://crates.io/percent-encoding/1.0.1 \
    crate://crates.io/percent-encoding/2.2.0 \
    crate://crates.io/pin-project-lite/0.2.9 \
    crate://crates.io/pin-utils/0.1.0 \
    crate://crates.io/pkg-config/0.3.26 \
    crate://crates.io/proc-macro2/0.4.30 \
    crate://crates.io/proc-macro2/1.0.47 \
    crate://crates.io/quick-error/1.2.3 \
    crate://crates.io/quote/0.6.13 \
    crate://crates.io/quote/1.0.21 \
    crate://crates.io/rand/0.5.6 \
    crate://crates.io/rand/0.6.5 \
    crate://crates.io/rand_chacha/0.1.1 \
    crate://crates.io/rand_core/0.3.1 \
    crate://crates.io/rand_core/0.4.2 \
    crate://crates.io/rand_hc/0.1.0 \
    crate://crates.io/rand_isaac/0.1.1 \
    crate://crates.io/rand_jitter/0.1.4 \
    crate://crates.io/rand_os/0.1.3 \
    crate://crates.io/rand_pcg/0.1.2 \
    crate://crates.io/rand_xorshift/0.1.1 \
    crate://crates.io/rdrand/0.4.0 \
    crate://crates.io/redox_syscall/0.1.57 \
    crate://crates.io/regex/1.6.0 \
    crate://crates.io/regex-syntax/0.6.27 \
    crate://crates.io/resolv-conf/0.6.3 \
    crate://crates.io/rustc-demangle/0.1.21 \
    crate://crates.io/rustc-hash/1.1.0 \
    crate://crates.io/rustc_version/0.2.3 \
    crate://crates.io/ryu/1.0.11 \
    crate://crates.io/scopeguard/0.3.3 \
    crate://crates.io/scopeguard/1.1.0 \
    crate://crates.io/semver/0.9.0 \
    crate://crates.io/semver-parser/0.7.0 \
    crate://crates.io/serde/1.0.147 \
    crate://crates.io/serde_derive/1.0.147 \
    crate://crates.io/serde_json/1.0.87 \
    crate://crates.io/serde_urlencoded/0.5.5 \
    crate://crates.io/sha1/0.6.1 \
    crate://crates.io/sha1_smol/1.0.0 \
    crate://crates.io/shlex/0.1.1 \
    crate://crates.io/signal-hook-registry/1.4.0 \
    crate://crates.io/slab/0.4.7 \
    crate://crates.io/smallvec/0.6.14 \
    crate://crates.io/socket2/0.3.19 \
    crate://crates.io/stable_deref_trait/1.2.0 \
    crate://crates.io/string/0.2.1 \
    crate://crates.io/strsim/0.8.0 \
    crate://crates.io/syn/0.15.44 \
    crate://crates.io/syn/1.0.103 \
    crate://crates.io/synstructure/0.12.6 \
    crate://crates.io/termcolor/1.1.3 \
    crate://crates.io/textwrap/0.11.0 \
    crate://crates.io/thiserror/1.0.23 \
    crate://crates.io/thiserror-impl/1.0.23 \
    crate://crates.io/time/0.1.44 \
    crate://crates.io/tinyvec/1.6.0 \
    crate://crates.io/tinyvec_macros/0.1.0 \
    crate://crates.io/tokio/0.1.22 \
    crate://crates.io/tokio-codec/0.1.2 \
    crate://crates.io/tokio-current-thread/0.1.7 \
    crate://crates.io/tokio-executor/0.1.10 \
    crate://crates.io/tokio-fs/0.1.7 \
    crate://crates.io/tokio-io/0.1.13 \
    crate://crates.io/tokio-openssl/0.2.1 \
    crate://crates.io/tokio-process/0.2.5 \
    crate://crates.io/tokio-reactor/0.1.12 \
    crate://crates.io/tokio-signal/0.2.9 \
    crate://crates.io/tokio-sync/0.1.8 \
    crate://crates.io/tokio-tcp/0.1.4 \
    crate://crates.io/tokio-threadpool/0.1.18 \
    crate://crates.io/tokio-timer/0.2.13 \
    crate://crates.io/tokio-udp/0.1.6 \
    crate://crates.io/tokio-uds/0.2.7 \
    crate://crates.io/tower-service/0.1.0 \
    crate://crates.io/trust-dns-proto/0.5.0 \
    crate://crates.io/trust-dns-resolver/0.10.0 \
    crate://crates.io/unicase/2.6.0 \
    crate://crates.io/unicode-bidi/0.3.8 \
    crate://crates.io/unicode-ident/1.0.5 \
    crate://crates.io/unicode-normalization/0.1.22 \
    crate://crates.io/unicode-width/0.1.10 \
    crate://crates.io/unicode-xid/0.1.0 \
    crate://crates.io/unicode-xid/0.2.4 \
    crate://crates.io/url/1.7.2 \
    crate://crates.io/uuid/0.7.4 \
    crate://crates.io/uuid/0.8.2 \
    crate://crates.io/vcpkg/0.2.15 \
    crate://crates.io/vec_map/0.8.2 \
    crate://crates.io/version_check/0.1.5 \
    crate://crates.io/version_check/0.9.4 \
    crate://crates.io/wasi/0.10.0+wasi-snapshot-preview1 \
    crate://crates.io/which/3.1.1 \
    crate://crates.io/widestring/0.2.2 \
    crate://crates.io/winapi/0.2.8 \
    crate://crates.io/winapi/0.3.9 \
    crate://crates.io/winapi-build/0.1.1 \
    crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi-util/0.1.5 \
    crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
    crate://crates.io/winreg/0.5.1 \
    crate://crates.io/ws2_32-sys/0.2.1 \
"

SRC_URI[actix-0.7.9.sha256sum] = "6c616db5fa4b0c40702fb75201c2af7f8aa8f3a2e2c1dda3b0655772aa949666"
SRC_URI[actix-net-0.2.5.sha256sum] = "12feb297c0a8b1ad2418d55227c61783111d40fbd49f927a09f8c67683471b8c"
SRC_URI[actix-web-0.7.16.sha256sum] = "9c1ae55616ff06c1d011c4e7f16f443b825df72aaf1c75e97cdc43a4ab83a602"
SRC_URI[actix_derive-0.3.2.sha256sum] = "4300e9431455322ae393d43a2ba1ef96b8080573c0fc23b196219efedfb6ba69"
SRC_URI[addr2line-0.17.0.sha256sum] = "b9ecd88a8c8378ca913a680cd98f0f13ac67383d35993f86c90a70e3f137816b"
SRC_URI[adler-1.0.2.sha256sum] = "f26201604c87b1e01bd3d98f8d5d9a8fcbb815e8cedb41ffccbeb4bf593a35fe"
SRC_URI[aho-corasick-0.7.19.sha256sum] = "b4f55bd91a0978cbfd91c457a164bab8b4001c833b7f323132c0a4e1922dd44e"
SRC_URI[ansi_term-0.12.1.sha256sum] = "d52a9bb7ec0cf484c551830a7ce27bd20d67eac647e1befb56b0be4ee39a55d2"
SRC_URI[askama_escape-0.1.0.sha256sum] = "719b48039ffac1564f67d70162109ba9341125cee0096a540e478355b3c724a7"
SRC_URI[atty-0.2.14.sha256sum] = "d9b39be18770d11421cdb1b9947a45dd3f37e93092cbf377614828a319d5fee8"
SRC_URI[autocfg-0.1.8.sha256sum] = "0dde43e75fd43e8a1bf86103336bc699aa8d17ad1be60c76c0bdfd4828e19b78"
SRC_URI[autocfg-1.1.0.sha256sum] = "d468802bab17cbc0cc575e9b053f41e72aa36bfa6b7f55e3529ffa43161b97fa"
SRC_URI[backtrace-0.3.66.sha256sum] = "cab84319d616cfb654d03394f38ab7e6f0919e181b1b57e1fd15e7fb4077d9a7"
SRC_URI[base64-0.10.1.sha256sum] = "0b25d992356d2eb0ed82172f5248873db5560c4721f564b13cb5193bda5e668e"
SRC_URI[bindgen-0.57.0.sha256sum] = "fd4865004a46a0aafb2a0a5eb19d3c9fc46ee5f063a6cfc605c69ac9ecf5263d"
SRC_URI[bitflags-1.3.2.sha256sum] = "bef38d45163c2f1dde094a7dfd33ccf595c92905c8f8f4fdc18d06fb1037718a"
SRC_URI[byteorder-1.4.3.sha256sum] = "14c189c53d098945499cdfa7ecc63567cf3886b3332b312a5b4585d8d3a6a610"
SRC_URI[bytes-0.4.12.sha256sum] = "206fdffcfa2df7cbe15601ef46c813fce0965eb3286db6b56c583b814b51c81c"
SRC_URI[cc-1.0.74.sha256sum] = "581f5dba903aac52ea3feb5ec4810848460ee833876f1f9b0fdeab1f19091574"
SRC_URI[cexpr-0.4.0.sha256sum] = "f4aedb84272dbe89af497cf81375129abda4fc0a9e7c5d317498c15cc30c0d27"
SRC_URI[cfg-if-0.1.10.sha256sum] = "4785bdd1c96b2a846b2bd7cc02e86b6b3dbf14e7e53446c4f54c92a361040822"
SRC_URI[cfg-if-1.0.0.sha256sum] = "baf1de4339761588bc0619e3cbc0120ee582ebb74b53b4efbf79117bd2da40fd"
SRC_URI[clang-sys-1.4.0.sha256sum] = "fa2e27ae6ab525c3d369ded447057bca5438d86dc3a68f6faafb8269ba82ebf3"
SRC_URI[clap-2.34.0.sha256sum] = "a0610544180c38b88101fecf2dd634b174a62eef6946f84dfc6a7127512b381c"
SRC_URI[cloudabi-0.0.3.sha256sum] = "ddfc5b9aa5d4507acaf872de71051dfd0e309860e88966e1051e462a077aac4f"
SRC_URI[cookie-0.11.5.sha256sum] = "be2018768ed1d848cc4d347d551546474025ba820e5db70e4c9aaa349f678bd7"
SRC_URI[crossbeam-channel-0.3.9.sha256sum] = "c8ec7fcd21571dc78f96cc96243cab8d8f035247c3efd16c687be154c3fa9efa"
SRC_URI[crossbeam-deque-0.7.4.sha256sum] = "c20ff29ded3204c5106278a81a38f4b482636ed4fa1e6cfbeef193291beb29ed"
SRC_URI[crossbeam-epoch-0.8.2.sha256sum] = "058ed274caafc1f60c4997b5fc07bf7dc7cca454af7c6e81edffe5f33f70dace"
SRC_URI[crossbeam-queue-0.1.2.sha256sum] = "7c979cd6cfe72335896575c6b5688da489e420d36a27a0b9eb0c73db574b4a4b"
SRC_URI[crossbeam-queue-0.2.3.sha256sum] = "774ba60a54c213d409d5353bda12d49cd68d14e45036a285234c8d6f91f92570"
SRC_URI[crossbeam-utils-0.6.6.sha256sum] = "04973fa96e96579258a5091af6003abde64af786b860f18622b82e026cca60e6"
SRC_URI[crossbeam-utils-0.7.2.sha256sum] = "c3c7c73a2d1e9fc0886a08b93e98eb643461230d5f1925e4036204d5f2e261a8"
SRC_URI[dtoa-0.4.8.sha256sum] = "56899898ce76aaf4a0f24d914c97ea6ed976d42fec6ad33fcbb0a1103e07b2b0"
SRC_URI[encoding-0.2.33.sha256sum] = "6b0d943856b990d12d3b55b359144ff341533e516d94098b1d3fc1ac666d36ec"
SRC_URI[encoding-index-japanese-1.20141219.5.sha256sum] = "04e8b2ff42e9a05335dbf8b5c6f7567e5591d0d916ccef4e0b1710d32a0d0c91"
SRC_URI[encoding-index-korean-1.20141219.5.sha256sum] = "4dc33fb8e6bcba213fe2f14275f0963fd16f0a02c878e3095ecfdf5bee529d81"
SRC_URI[encoding-index-simpchinese-1.20141219.5.sha256sum] = "d87a7194909b9118fc707194baa434a4e3b0fb6a5a757c73c3adb07aa25031f7"
SRC_URI[encoding-index-singlebyte-1.20141219.5.sha256sum] = "3351d5acffb224af9ca265f435b859c7c01537c0849754d3db3fdf2bfe2ae84a"
SRC_URI[encoding-index-tradchinese-1.20141219.5.sha256sum] = "fd0e20d5688ce3cab59eb3ef3a2083a5c77bf496cb798dc6fcdb75f323890c18"
SRC_URI[encoding_index_tests-0.1.4.sha256sum] = "a246d82be1c9d791c5dfde9a2bd045fc3cbba3fa2b11ad558f27d01712f00569"
SRC_URI[env_logger-0.6.2.sha256sum] = "aafcde04e90a5226a6443b7aabdb016ba2f8307c847d524724bd9b346dd1a2d3"
SRC_URI[env_logger-0.8.4.sha256sum] = "a19187fea3ac7e84da7dacf48de0c45d63c6a76f9490dae389aead16c243fce3"
SRC_URI[error-chain-0.8.1.sha256sum] = "6930e04918388a9a2e41d518c25cf679ccafe26733fb4127dbf21993f2575d46"
SRC_URI[failure-0.1.8.sha256sum] = "d32e9bd16cc02eae7db7ef620b392808b89f6a5e16bb3497d159c6b92a0f4f86"
SRC_URI[failure_derive-0.1.8.sha256sum] = "aa4da3c766cd7a0db8242e326e9e4e081edd567072893ed320008189715366a4"
SRC_URI[fnv-1.0.7.sha256sum] = "3f9eec918d3f24069decb9af1554cad7c880e2da24a9afd88aca000531ab82c1"
SRC_URI[foreign-types-0.3.2.sha256sum] = "f6f339eb8adc052cd2ca78910fda869aefa38d22d5cb648e6485e4d3fc06f3b1"
SRC_URI[foreign-types-shared-0.1.1.sha256sum] = "00b0228411908ca8685dba7fc2cdd70ec9990a6e753e89b6ac91a84c40fbaf4b"
SRC_URI[fuchsia-cprng-0.1.1.sha256sum] = "a06f77d526c1a601b7c4cdd98f54b5eaabffc14d5f2f0296febdc7f357c6d3ba"
SRC_URI[fuchsia-zircon-0.3.3.sha256sum] = "2e9763c69ebaae630ba35f74888db465e49e259ba1bc0eda7d06f4a067615d82"
SRC_URI[fuchsia-zircon-sys-0.3.3.sha256sum] = "3dcaa9ae7725d12cdb85b3ad99a434db70b468c09ded17e012d86b5c1010f7a7"
SRC_URI[futures-0.1.31.sha256sum] = "3a471a38ef8ed83cd6e40aa59c1ffe17db6855c18e3604d9c4ed8c08ebc28678"
SRC_URI[futures-0.3.25.sha256sum] = "38390104763dc37a5145a53c29c63c1290b5d316d6086ec32c293f6736051bb0"
SRC_URI[futures-channel-0.3.25.sha256sum] = "52ba265a92256105f45b719605a571ffe2d1f0fea3807304b522c1d778f79eed"
SRC_URI[futures-core-0.3.25.sha256sum] = "04909a7a7e4633ae6c4a9ab280aeb86da1236243a77b694a49eacd659a4bd3ac"
SRC_URI[futures-cpupool-0.1.8.sha256sum] = "ab90cde24b3319636588d0c35fe03b1333857621051837ed769faefb4c2162e4"
SRC_URI[futures-executor-0.3.25.sha256sum] = "7acc85df6714c176ab5edf386123fafe217be88c0840ec11f199441134a074e2"
SRC_URI[futures-io-0.3.25.sha256sum] = "00f5fb52a06bdcadeb54e8d3671f8888a39697dcb0b81b23b55174030427f4eb"
SRC_URI[futures-macro-0.3.25.sha256sum] = "bdfb8ce053d86b91919aad980c220b1fb8401a9394410e1c289ed7e66b61835d"
SRC_URI[futures-sink-0.3.25.sha256sum] = "39c15cf1a4aa79df40f1bb462fb39676d0ad9e366c2a33b590d7c66f4f81fcf9"
SRC_URI[futures-task-0.3.25.sha256sum] = "2ffb393ac5d9a6eaa9d3fdf37ae2776656b706e200c8e16b1bdb227f5198e6ea"
SRC_URI[futures-util-0.3.25.sha256sum] = "197676987abd2f9cadff84926f410af1c183608d36641465df73ae8211dc65d6"
SRC_URI[gimli-0.26.2.sha256sum] = "22030e2c5a68ec659fde1e949a745124b48e6fa8b045b7ed5bd1fe4ccc5c4e5d"
SRC_URI[glob-0.3.0.sha256sum] = "9b919933a397b79c37e33b77bb2aa3dc8eb6e165ad809e58ff75bc7db2e34574"
SRC_URI[h2-0.1.26.sha256sum] = "a5b34c246847f938a410a03c5458c7fee2274436675e76d8b903c08efc29c462"
SRC_URI[hashbrown-0.12.3.sha256sum] = "8a9ee70c43aaf417c914396645a0fa852624801b24ebb7ae78fe8272889ac888"
SRC_URI[hermit-abi-0.1.19.sha256sum] = "62b467343b94ba476dcb2500d242dadbb39557df889310ac77c5d99100aaac33"
SRC_URI[hostname-0.3.1.sha256sum] = "3c731c3e10504cc8ed35cfe2f1db4c9274c3d35fa486e3b31df46f068ef3e867"
SRC_URI[http-0.1.21.sha256sum] = "d6ccf5ede3a895d8856620237b2f02972c1bbc78d2965ad7fe8838d4a0ed41f0"
SRC_URI[httparse-1.8.0.sha256sum] = "d897f394bad6a705d5f4104762e116a75639e470d80901eed05a860a95cb1904"
SRC_URI[humantime-1.3.0.sha256sum] = "df004cfca50ef23c36850aaaa59ad52cc70d0e90243c3c7737a4dd32dc7a3c4f"
SRC_URI[humantime-2.1.0.sha256sum] = "9a3a5bfb195931eeb336b2a7b4d761daec841b97f947d34394601737a7bba5e4"
SRC_URI[idna-0.1.5.sha256sum] = "38f09e0f0b1fb55fdee1f17470ad800da77af5186a1a76c026b679358b7e844e"
SRC_URI[indexmap-1.9.1.sha256sum] = "10a35a97730320ffe8e2d410b5d3b69279b98d2c14bdb8b70ea89ecf7888d41e"
SRC_URI[iovec-0.1.4.sha256sum] = "b2b3ea6ff95e175473f8ffe6a7eb7c00d054240321b84c57051175fe3c1e075e"
SRC_URI[ipconfig-0.1.9.sha256sum] = "08f7eadeaf4b52700de180d147c4805f199854600b36faa963d91114827b2ffc"
SRC_URI[itoa-0.4.8.sha256sum] = "b71991ff56294aa922b450139ee08b3bfc70982c6b2c7562771375cf73542dd4"
SRC_URI[itoa-1.0.4.sha256sum] = "4217ad341ebadf8d8e724e264f13e593e0648f5b3e94b3896a5df283be015ecc"
SRC_URI[kernel32-sys-0.2.2.sha256sum] = "7507624b29483431c0ba2d82aece8ca6cdba9382bff4ddd0f7490560c056098d"
SRC_URI[language-tags-0.2.2.sha256sum] = "a91d884b6667cd606bb5a69aa0c99ba811a115fc68915e7056ec08a46e93199a"
SRC_URI[lazy_static-1.4.0.sha256sum] = "e2abad23fbc42b3700f2f279844dc832adb2b2eb069b2df918f455c4e18cc646"
SRC_URI[lazycell-1.3.0.sha256sum] = "830d08ce1d1d941e6b30645f1a0eb5643013d835ce3779a5fc208261dbe10f55"
SRC_URI[libc-0.2.137.sha256sum] = "fc7fcc620a3bff7cdd7a365be3376c97191aeaccc2a603e600951e452615bf89"
SRC_URI[libloading-0.7.3.sha256sum] = "efbc0f03f9a775e9f6aed295c6a1ba2253c5757a9e03d55c6caa46a681abcddd"
SRC_URI[linked-hash-map-0.5.6.sha256sum] = "0717cef1bc8b636c6e1c1bbdefc09e6322da8a9321966e8928ef80d20f7f770f"
SRC_URI[listenfd-0.3.5.sha256sum] = "809e514e2cb8a9624701346ea3e694c1766d76778e343e537d873c1c366e79a7"
SRC_URI[lock_api-0.1.5.sha256sum] = "62ebf1391f6acad60e5c8b43706dde4582df75c06698ab44511d15016bc2442c"
SRC_URI[lock_api-0.3.4.sha256sum] = "c4da24a77a3d8a6d4862d95f72e6fdb9c09a643ecdb402d754004a557f2bec75"
SRC_URI[log-0.4.17.sha256sum] = "abb12e687cfb44aa40f41fc3978ef76448f9b6038cad6aef4259d3c095a2382e"
SRC_URI[lru-cache-0.1.2.sha256sum] = "31e24f1ad8321ca0e8a1e0ac13f23cb668e6f5466c2c57319f6a5cf1cc8e3b1c"
SRC_URI[match_cfg-0.1.0.sha256sum] = "ffbee8634e0d45d258acb448e7eaab3fce7a0a467395d4d9f228e3c1f01fb2e4"
SRC_URI[matches-0.1.9.sha256sum] = "a3e378b66a060d48947b590737b30a1be76706c8dd7b8ba0f2fe3989c68a853f"
SRC_URI[maybe-uninit-2.0.0.sha256sum] = "60302e4db3a61da70c0cb7991976248362f30319e88850c487b9b95bbf059e00"
SRC_URI[memchr-2.5.0.sha256sum] = "2dffe52ecf27772e601905b7522cb4ef790d2cc203488bbd0e2fe85fcb74566d"
SRC_URI[memoffset-0.5.6.sha256sum] = "043175f069eda7b85febe4a74abbaeff828d9f8b448515d3151a14a3542811aa"
SRC_URI[mime-0.3.16.sha256sum] = "2a60c7ce501c71e03a9c9c0d35b861413ae925bd979cc7a4e30d060069aaac8d"
SRC_URI[mime_guess-2.0.4.sha256sum] = "4192263c238a5f0d0c6bfd21f336a313a4ce1c450542449ca191bb657b4642ef"
SRC_URI[miniz_oxide-0.5.4.sha256sum] = "96590ba8f175222643a85693f33d26e9c8a015f599c216509b1a6894af675d34"
SRC_URI[mio-0.6.23.sha256sum] = "4afd66f5b91bf2a3bc13fad0e21caedac168ca4c707504e75585648ae80e4cc4"
SRC_URI[mio-named-pipes-0.1.7.sha256sum] = "0840c1c50fd55e521b247f949c241c9997709f23bd7f023b9762cd561e935656"
SRC_URI[mio-uds-0.6.8.sha256sum] = "afcb699eb26d4332647cc848492bbc15eafb26f08d0304550d5aa1f612e066f0"
SRC_URI[miow-0.2.2.sha256sum] = "ebd808424166322d4a38da87083bfddd3ac4c131334ed55856112eb06d46944d"
SRC_URI[miow-0.3.7.sha256sum] = "b9f1c5b025cda876f66ef43a113f91ebc9f4ccef34843000e0adf6ebbab84e21"
SRC_URI[net2-0.2.38.sha256sum] = "74d0df99cfcd2530b2e694f6e17e7f37b8e26bb23983ac530c0c97408837c631"
SRC_URI[nom-5.1.2.sha256sum] = "ffb4262d26ed83a1c0a33a38fe2bb15797329c85770da05e6b828ddb782627af"
SRC_URI[num_cpus-1.13.1.sha256sum] = "19e64526ebdee182341572e50e9ad03965aa510cd94427a4549448f285e957a1"
SRC_URI[object-0.29.0.sha256sum] = "21158b2c33aa6d4561f1c0a6ea283ca92bc54802a93b263e910746d679a7eb53"
SRC_URI[once_cell-1.16.0.sha256sum] = "86f0b0d4bf799edbc74508c1e8bf170ff5f41238e5f8225603ca7caaae2b7860"
SRC_URI[openssl-0.10.42.sha256sum] = "12fc0523e3bd51a692c8850d075d74dc062ccf251c0110668cbd921917118a13"
SRC_URI[openssl-macros-0.1.0.sha256sum] = "b501e44f11665960c7e7fcf062c7d96a14ade4aa98116c004b2e37b5be7d736c"
SRC_URI[openssl-sys-0.9.77.sha256sum] = "b03b84c3b2d099b81f0953422b4d4ad58761589d0229b5506356afca05a3670a"
SRC_URI[owning_ref-0.4.1.sha256sum] = "6ff55baddef9e4ad00f88b6c743a2a8062d4c6ade126c2a528644b8e444d52ce"
SRC_URI[parking_lot-0.7.1.sha256sum] = "ab41b4aed082705d1056416ae4468b6ea99d52599ecf3169b00088d43113e337"
SRC_URI[parking_lot-0.9.0.sha256sum] = "f842b1982eb6c2fe34036a4fbfb06dd185a3f5c8edfaacdf7d1ea10b07de6252"
SRC_URI[parking_lot_core-0.4.0.sha256sum] = "94c8c7923936b28d546dfd14d4472eaf34c99b14e1c973a32b3e6d4eb04298c9"
SRC_URI[parking_lot_core-0.6.2.sha256sum] = "b876b1b9e7ac6e1a74a6da34d25c42e17e8862aa409cbbbdcfc8d86c6f3bc62b"
SRC_URI[peeking_take_while-0.1.2.sha256sum] = "19b17cddbe7ec3f8bc800887bab5e717348c95ea2ca0b1bf0837fb964dc67099"
SRC_URI[percent-encoding-1.0.1.sha256sum] = "31010dd2e1ac33d5b46a5b413495239882813e0369f8ed8a5e266f173602f831"
SRC_URI[percent-encoding-2.2.0.sha256sum] = "478c572c3d73181ff3c2539045f6eb99e5491218eae919370993b890cdbdd98e"
SRC_URI[pin-project-lite-0.2.9.sha256sum] = "e0a7ae3ac2f1173085d398531c705756c94a4c56843785df85a60c1a0afac116"
SRC_URI[pin-utils-0.1.0.sha256sum] = "8b870d8c151b6f2fb93e84a13146138f05d02ed11c7e7c54f8826aaaf7c9f184"
SRC_URI[pkg-config-0.3.26.sha256sum] = "6ac9a59f73473f1b8d852421e59e64809f025994837ef743615c6d0c5b305160"
SRC_URI[proc-macro2-0.4.30.sha256sum] = "cf3d2011ab5c909338f7887f4fc896d35932e29146c12c8d01da6b22a80ba759"
SRC_URI[proc-macro2-1.0.47.sha256sum] = "5ea3d908b0e36316caf9e9e2c4625cdde190a7e6f440d794667ed17a1855e725"
SRC_URI[quick-error-1.2.3.sha256sum] = "a1d01941d82fa2ab50be1e79e6714289dd7cde78eba4c074bc5a4374f650dfe0"
SRC_URI[quote-0.6.13.sha256sum] = "6ce23b6b870e8f94f81fb0a363d65d86675884b34a09043c81e5562f11c1f8e1"
SRC_URI[quote-1.0.21.sha256sum] = "bbe448f377a7d6961e30f5955f9b8d106c3f5e449d493ee1b125c1d43c2b5179"
SRC_URI[rand-0.5.6.sha256sum] = "c618c47cd3ebd209790115ab837de41425723956ad3ce2e6a7f09890947cacb9"
SRC_URI[rand-0.6.5.sha256sum] = "6d71dacdc3c88c1fde3885a3be3fbab9f35724e6ce99467f7d9c5026132184ca"
SRC_URI[rand_chacha-0.1.1.sha256sum] = "556d3a1ca6600bfcbab7c7c91ccb085ac7fbbcd70e008a98742e7847f4f7bcef"
SRC_URI[rand_core-0.3.1.sha256sum] = "7a6fdeb83b075e8266dcc8762c22776f6877a63111121f5f8c7411e5be7eed4b"
SRC_URI[rand_core-0.4.2.sha256sum] = "9c33a3c44ca05fa6f1807d8e6743f3824e8509beca625669633be0acbdf509dc"
SRC_URI[rand_hc-0.1.0.sha256sum] = "7b40677c7be09ae76218dc623efbf7b18e34bced3f38883af07bb75630a21bc4"
SRC_URI[rand_isaac-0.1.1.sha256sum] = "ded997c9d5f13925be2a6fd7e66bf1872597f759fd9dd93513dd7e92e5a5ee08"
SRC_URI[rand_jitter-0.1.4.sha256sum] = "1166d5c91dc97b88d1decc3285bb0a99ed84b05cfd0bc2341bdf2d43fc41e39b"
SRC_URI[rand_os-0.1.3.sha256sum] = "7b75f676a1e053fc562eafbb47838d67c84801e38fc1ba459e8f180deabd5071"
SRC_URI[rand_pcg-0.1.2.sha256sum] = "abf9b09b01790cfe0364f52bf32995ea3c39f4d2dd011eac241d2914146d0b44"
SRC_URI[rand_xorshift-0.1.1.sha256sum] = "cbf7e9e623549b0e21f6e97cf8ecf247c1a8fd2e8a992ae265314300b2455d5c"
SRC_URI[rdrand-0.4.0.sha256sum] = "678054eb77286b51581ba43620cc911abf02758c91f93f479767aed0f90458b2"
SRC_URI[redox_syscall-0.1.57.sha256sum] = "41cc0f7e4d5d4544e8861606a285bb08d3e70712ccc7d2b84d7c0ccfaf4b05ce"
SRC_URI[regex-1.6.0.sha256sum] = "4c4eb3267174b8c6c2f654116623910a0fef09c4753f8dd83db29c48a0df988b"
SRC_URI[regex-syntax-0.6.27.sha256sum] = "a3f87b73ce11b1619a3c6332f45341e0047173771e8b8b73f87bfeefb7b56244"
SRC_URI[resolv-conf-0.6.3.sha256sum] = "11834e137f3b14e309437a8276714eed3a80d1ef894869e510f2c0c0b98b9f4a"
SRC_URI[rustc-demangle-0.1.21.sha256sum] = "7ef03e0a2b150c7a90d01faf6254c9c48a41e95fb2a8c2ac1c6f0d2b9aefc342"
SRC_URI[rustc-hash-1.1.0.sha256sum] = "08d43f7aa6b08d49f382cde6a7982047c3426db949b1424bc4b7ec9ae12c6ce2"
SRC_URI[rustc_version-0.2.3.sha256sum] = "138e3e0acb6c9fb258b19b67cb8abd63c00679d2851805ea151465464fe9030a"
SRC_URI[ryu-1.0.11.sha256sum] = "4501abdff3ae82a1c1b477a17252eb69cee9e66eb915c1abaa4f44d873df9f09"
SRC_URI[scopeguard-0.3.3.sha256sum] = "94258f53601af11e6a49f722422f6e3425c52b06245a5cf9bc09908b174f5e27"
SRC_URI[scopeguard-1.1.0.sha256sum] = "d29ab0c6d3fc0ee92fe66e2d99f700eab17a8d57d1c1d3b748380fb20baa78cd"
SRC_URI[semver-0.9.0.sha256sum] = "1d7eb9ef2c18661902cc47e535f9bc51b78acd254da71d375c2f6720d9a40403"
SRC_URI[semver-parser-0.7.0.sha256sum] = "388a1df253eca08550bef6c72392cfe7c30914bf41df5269b68cbd6ff8f570a3"
SRC_URI[serde-1.0.147.sha256sum] = "d193d69bae983fc11a79df82342761dfbf28a99fc8d203dca4c3c1b590948965"
SRC_URI[serde_derive-1.0.147.sha256sum] = "4f1d362ca8fc9c3e3a7484440752472d68a6caa98f1ab81d99b5dfe517cec852"
SRC_URI[serde_json-1.0.87.sha256sum] = "6ce777b7b150d76b9cf60d28b55f5847135a003f7d7350c6be7a773508ce7d45"
SRC_URI[serde_urlencoded-0.5.5.sha256sum] = "642dd69105886af2efd227f75a520ec9b44a820d65bc133a9131f7d229fd165a"
SRC_URI[sha1-0.6.1.sha256sum] = "c1da05c97445caa12d05e848c4a4fcbbea29e748ac28f7e80e9b010392063770"
SRC_URI[sha1_smol-1.0.0.sha256sum] = "ae1a47186c03a32177042e55dbc5fd5aee900b8e0069a8d70fba96a9375cd012"
SRC_URI[shlex-0.1.1.sha256sum] = "7fdf1b9db47230893d76faad238fd6097fd6d6a9245cd7a4d90dbd639536bbd2"
SRC_URI[signal-hook-registry-1.4.0.sha256sum] = "e51e73328dc4ac0c7ccbda3a494dfa03df1de2f46018127f60c693f2648455b0"
SRC_URI[slab-0.4.7.sha256sum] = "4614a76b2a8be0058caa9dbbaf66d988527d86d003c11a94fbd335d7661edcef"
SRC_URI[smallvec-0.6.14.sha256sum] = "b97fcaeba89edba30f044a10c6a3cc39df9c3f17d7cd829dd1446cab35f890e0"
SRC_URI[socket2-0.3.19.sha256sum] = "122e570113d28d773067fab24266b66753f6ea915758651696b6e35e49f88d6e"
SRC_URI[stable_deref_trait-1.2.0.sha256sum] = "a8f112729512f8e442d81f95a8a7ddf2b7c6b8a1a6f509a95864142b30cab2d3"
SRC_URI[string-0.2.1.sha256sum] = "d24114bfcceb867ca7f71a0d3fe45d45619ec47a6fbfa98cb14e14250bfa5d6d"
SRC_URI[strsim-0.8.0.sha256sum] = "8ea5119cdb4c55b55d432abb513a0429384878c15dde60cc77b1c99de1a95a6a"
SRC_URI[syn-0.15.44.sha256sum] = "9ca4b3b69a77cbe1ffc9e198781b7acb0c7365a883670e8f1c1bc66fba79a5c5"
SRC_URI[syn-1.0.103.sha256sum] = "a864042229133ada95abf3b54fdc62ef5ccabe9515b64717bcb9a1919e59445d"
SRC_URI[synstructure-0.12.6.sha256sum] = "f36bdaa60a83aca3921b5259d5400cbf5e90fc51931376a9bd4a0eb79aa7210f"
SRC_URI[termcolor-1.1.3.sha256sum] = "bab24d30b911b2376f3a13cc2cd443142f0c81dda04c118693e35b3835757755"
SRC_URI[textwrap-0.11.0.sha256sum] = "d326610f408c7a4eb6f51c37c330e496b08506c9457c9d34287ecc38809fb060"
SRC_URI[thiserror-1.0.23.sha256sum] = "76cc616c6abf8c8928e2fdcc0dbfab37175edd8fb49a4641066ad1364fdab146"
SRC_URI[thiserror-impl-1.0.23.sha256sum] = "9be73a2caec27583d0046ef3796c3794f868a5bc813db689eed00c7631275cd1"
SRC_URI[time-0.1.44.sha256sum] = "6db9e6914ab8b1ae1c260a4ae7a49b6c5611b40328a735b21862567685e73255"
SRC_URI[tinyvec-1.6.0.sha256sum] = "87cc5ceb3875bb20c2890005a4e226a4651264a5c75edb2421b52861a0a0cb50"
SRC_URI[tinyvec_macros-0.1.0.sha256sum] = "cda74da7e1a664f795bb1f8a87ec406fb89a02522cf6e50620d016add6dbbf5c"
SRC_URI[tokio-0.1.22.sha256sum] = "5a09c0b5bb588872ab2f09afa13ee6e9dac11e10a0ec9e8e3ba39a5a5d530af6"
SRC_URI[tokio-codec-0.1.2.sha256sum] = "25b2998660ba0e70d18684de5d06b70b70a3a747469af9dea7618cc59e75976b"
SRC_URI[tokio-current-thread-0.1.7.sha256sum] = "b1de0e32a83f131e002238d7ccde18211c0a5397f60cbfffcb112868c2e0e20e"
SRC_URI[tokio-executor-0.1.10.sha256sum] = "fb2d1b8f4548dbf5e1f7818512e9c406860678f29c300cdf0ebac72d1a3a1671"
SRC_URI[tokio-fs-0.1.7.sha256sum] = "297a1206e0ca6302a0eed35b700d292b275256f596e2f3fea7729d5e629b6ff4"
SRC_URI[tokio-io-0.1.13.sha256sum] = "57fc868aae093479e3131e3d165c93b1c7474109d13c90ec0dda2a1bbfff0674"
SRC_URI[tokio-openssl-0.2.1.sha256sum] = "4646ae1fd623393de3d796ea53af75acd02938dd5579544fbd6d236d041978a6"
SRC_URI[tokio-process-0.2.5.sha256sum] = "382d90f43fa31caebe5d3bc6cfd854963394fff3b8cb59d5146607aaae7e7e43"
SRC_URI[tokio-reactor-0.1.12.sha256sum] = "09bc590ec4ba8ba87652da2068d150dcada2cfa2e07faae270a5e0409aa51351"
SRC_URI[tokio-signal-0.2.9.sha256sum] = "d0c34c6e548f101053321cba3da7cbb87a610b85555884c41b07da2eb91aff12"
SRC_URI[tokio-sync-0.1.8.sha256sum] = "edfe50152bc8164fcc456dab7891fa9bf8beaf01c5ee7e1dd43a397c3cf87dee"
SRC_URI[tokio-tcp-0.1.4.sha256sum] = "98df18ed66e3b72e742f185882a9e201892407957e45fbff8da17ae7a7c51f72"
SRC_URI[tokio-threadpool-0.1.18.sha256sum] = "df720b6581784c118f0eb4310796b12b1d242a7eb95f716a8367855325c25f89"
SRC_URI[tokio-timer-0.2.13.sha256sum] = "93044f2d313c95ff1cb7809ce9a7a05735b012288a888b62d4434fd58c94f296"
SRC_URI[tokio-udp-0.1.6.sha256sum] = "e2a0b10e610b39c38b031a2fcab08e4b82f16ece36504988dcbd81dbba650d82"
SRC_URI[tokio-uds-0.2.7.sha256sum] = "ab57a4ac4111c8c9dbcf70779f6fc8bc35ae4b2454809febac840ad19bd7e4e0"
SRC_URI[tower-service-0.1.0.sha256sum] = "b32f72af77f1bfe3d3d4da8516a238ebe7039b51dd8637a09841ac7f16d2c987"
SRC_URI[trust-dns-proto-0.5.0.sha256sum] = "0838272e89f1c693b4df38dc353412e389cf548ceed6f9fd1af5a8d6e0e7cf74"
SRC_URI[trust-dns-resolver-0.10.0.sha256sum] = "4e913a5df94658858e548cc95a3212797ee524e487ede091c32f27ca26e11620"
SRC_URI[unicase-2.6.0.sha256sum] = "50f37be617794602aabbeee0be4f259dc1778fabe05e2d67ee8f79326d5cb4f6"
SRC_URI[unicode-bidi-0.3.8.sha256sum] = "099b7128301d285f79ddd55b9a83d5e6b9e97c92e0ea0daebee7263e932de992"
SRC_URI[unicode-ident-1.0.5.sha256sum] = "6ceab39d59e4c9499d4e5a8ee0e2735b891bb7308ac83dfb4e80cad195c9f6f3"
SRC_URI[unicode-normalization-0.1.22.sha256sum] = "5c5713f0fc4b5db668a2ac63cdb7bb4469d8c9fed047b1d0292cc7b0ce2ba921"
SRC_URI[unicode-width-0.1.10.sha256sum] = "c0edd1e5b14653f783770bce4a4dabb4a5108a5370a5f5d8cfe8710c361f6c8b"
SRC_URI[unicode-xid-0.1.0.sha256sum] = "fc72304796d0818e357ead4e000d19c9c174ab23dc11093ac919054d20a6a7fc"
SRC_URI[unicode-xid-0.2.4.sha256sum] = "f962df74c8c05a667b5ee8bcf162993134c104e96440b663c8daa176dc772d8c"
SRC_URI[url-1.7.2.sha256sum] = "dd4e7c0d531266369519a4aa4f399d748bd37043b00bde1e4ff1f60a120b355a"
SRC_URI[uuid-0.7.4.sha256sum] = "90dbc611eb48397705a6b0f6e917da23ae517e4d127123d2cf7674206627d32a"
SRC_URI[uuid-0.8.2.sha256sum] = "bc5cf98d8186244414c848017f0e2676b3fcb46807f6668a97dfe67359a3c4b7"
SRC_URI[vcpkg-0.2.15.sha256sum] = "accd4ea62f7bb7a82fe23066fb0957d48ef677f6eeb8215f372f52e48bb32426"
SRC_URI[vec_map-0.8.2.sha256sum] = "f1bddf1187be692e79c5ffeab891132dfb0f236ed36a43c7ed39f1165ee20191"
SRC_URI[version_check-0.1.5.sha256sum] = "914b1a6776c4c929a602fafd8bc742e06365d4bcbe48c30f9cca5824f70dc9dd"
SRC_URI[version_check-0.9.4.sha256sum] = "49874b5167b65d7193b8aba1567f5c7d93d001cafc34600cee003eda787e483f"
SRC_URI[wasi-0.10.0+wasi-snapshot-preview1.sha256sum] = "1a143597ca7c7793eff794def352d41792a93c481eb1042423ff7ff72ba2c31f"
SRC_URI[which-3.1.1.sha256sum] = "d011071ae14a2f6671d0b74080ae0cd8ebf3a6f8c9589a2cd45f23126fe29724"
SRC_URI[widestring-0.2.2.sha256sum] = "7157704c2e12e3d2189c507b7482c52820a16dfa4465ba91add92f266667cadb"
SRC_URI[winapi-0.2.8.sha256sum] = "167dc9d6949a9b857f3451275e911c3f44255842c1f7a76f33c55103a909087a"
SRC_URI[winapi-0.3.9.sha256sum] = "5c839a674fcd7a98952e593242ea400abe93992746761e38641405d28b00f419"
SRC_URI[winapi-build-0.1.1.sha256sum] = "2d315eee3b34aca4797b2da6b13ed88266e6d612562a0c46390af8299fc699bc"
SRC_URI[winapi-i686-pc-windows-gnu-0.4.0.sha256sum] = "ac3b87c63620426dd9b991e5ce0329eff545bccbbb34f3be09ff6fb6ab51b7b6"
SRC_URI[winapi-util-0.1.5.sha256sum] = "70ec6ce85bb158151cae5e5c87f95a8e97d2c0c4b001223f33a334e3ce5de178"
SRC_URI[winapi-x86_64-pc-windows-gnu-0.4.0.sha256sum] = "712e227841d057c1ee1cd2fb22fa7e5a5461ae8e48fa2ca79ec42cfc1931183f"
SRC_URI[winreg-0.5.1.sha256sum] = "a27a759395c1195c4cc5cda607ef6f8f6498f64e78f7900f5de0a127a424704a"
SRC_URI[ws2_32-sys-0.2.1.sha256sum] = "d59cefebd0c892fa2dd6de581e937301d8552cb44489cdff035c6187cb63fa5e"
