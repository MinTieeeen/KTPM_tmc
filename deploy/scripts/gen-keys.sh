#!/usr/bin/env bash
# Sinh khóa cho môi trường demo vào deploy/env/secrets/ (thư mục này nằm trong .gitignore):
#   - Ed25519 ký mã QR (C2): in ra TMC_QR_SIGNING_KEY / TMC_QR_PUBLIC_KEY (base64 DER)
#   - RSA 2048 ký JWT RS256 (C7)
# Không chạy script này thì profile demo của central tự sinh khóa mỗi lần khởi động
# (token đăng nhập cũ sẽ mất hiệu lực sau khi central khởi động lại).
# Yêu cầu: openssl 1.1.1+
set -euo pipefail
dir="$(cd "$(dirname "$0")/../env/secrets" && pwd)"

openssl genpkey -algorithm ed25519 -out "$dir/qr-signing.pem"
openssl pkey -in "$dir/qr-signing.pem" -pubout -out "$dir/qr-public.pem"
priv_b64="$(openssl pkcs8 -topk8 -nocrypt -in "$dir/qr-signing.pem" -outform DER | base64 | tr -d '\n')"
pub_b64="$(openssl pkey -in "$dir/qr-signing.pem" -pubout -outform DER | base64 | tr -d '\n')"

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$dir/jwt-private.pem"
openssl pkey -in "$dir/jwt-private.pem" -pubout -out "$dir/jwt-public.pem"

cat > "$dir/central.secrets.env" <<EOF
TMC_QR_SIGNING_KEY=${priv_b64}
TMC_QR_PUBLIC_KEY=${pub_b64}
JWT_PRIVATE_KEY_PATH=/run/secrets/jwt-private.pem
JWT_PUBLIC_KEY_PATH=/run/secrets/jwt-public.pem
EOF

echo "Đã sinh khóa trong $dir"
echo "Để dùng khóa cố định: thêm env/secrets/central.secrets.env vào env_file của central"
echo "và mount env/secrets vào /run/secrets (xem chú thích trong docker-compose.yml)."
