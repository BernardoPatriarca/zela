#!/usr/bin/env bash
set -euo pipefail

PERFIL=zela
REGIAO=sa-east-1
STACK=zela-frontend

cd "$(dirname "$0")/.."

saida() {
  aws cloudformation describe-stacks \
    --stack-name "$STACK" --region "$REGIAO" --profile "$PERFIL" \
    --query "Stacks[0].Outputs[?OutputKey=='$1'].OutputValue" --output text
}

BUCKET=$(saida BucketName)
DISTRIBUICAO=$(saida DistributionId)

npx ng build --configuration production

DESTINO="dist/zela/browser"

aws s3 sync "$DESTINO" "s3://$BUCKET" \
  --profile "$PERFIL" --region "$REGIAO" \
  --exclude "*" --include "*.js" --include "*.css" \
  --cache-control "public,max-age=31536000,immutable"

aws s3 sync "$DESTINO" "s3://$BUCKET" \
  --profile "$PERFIL" --region "$REGIAO" \
  --exclude "*.js" --exclude "*.css" --delete \
  --cache-control "no-cache"

aws cloudfront create-invalidation \
  --distribution-id "$DISTRIBUICAO" --paths "/*" --profile "$PERFIL"

echo "https://$(aws cloudfront get-distribution --id "$DISTRIBUICAO" --profile "$PERFIL" --query 'Distribution.DomainName' --output text)"