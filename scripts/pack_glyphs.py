#!/usr/bin/env python3
import argparse
import json
from pathlib import Path
from typing import Dict, List, Tuple

from PIL import Image

def load_widths(path: Path) -> Dict[str, int]:
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)

def gather_glyphs(input_dir: Path) -> List[Path]:
    return sorted(p for p in input_dir.glob("*.png") if p.is_file())

def compute_grid(sheet_width: int, sheet_height: int, tile_size: int) -> Tuple[int, int, int]:
    cols = sheet_width // tile_size
    rows = sheet_height // tile_size
    if cols == 0 or rows == 0:
        raise ValueError("Sheet size too small for at least one tile")
    capacity = cols * rows
    return cols, rows, capacity

def pack_glyphs(
    input_dir: Path,
    widths_path: Path,
    output_dir: Path,
    charmap_path: Path,
    sheet_width: int,
    sheet_height: int,
    tile_size: int,
) -> None:
    widths = load_widths(widths_path)
    glyph_paths = gather_glyphs(input_dir)
    if not glyph_paths:
        raise FileNotFoundError(f"No PNGs found in {input_dir}")

    output_dir.mkdir(parents=True, exist_ok=True)
    cols, rows, capacity = compute_grid(sheet_width, sheet_height, tile_size)

    sheets: List[Image.Image] = []
    charmap: Dict[str, Dict[str, Dict[str, int]]] = {}

    for idx, glyph_path in enumerate(glyph_paths):
        sheet_index = idx // capacity
        pos_in_sheet = idx % capacity
        col = pos_in_sheet % cols
        row = pos_in_sheet // cols
        x = col * tile_size
        y = row * tile_size

        if pos_in_sheet == 0:
            img = Image.new("RGBA", (sheet_width, sheet_height), (0, 0, 0, 0))
            sheets.append(img)

        key = glyph_path.stem

        glyph_img = Image.open(glyph_path).convert("RGBA")
        if glyph_img.size != (tile_size, tile_size):
            if key == "u0020":
                glyph_img = Image.new("RGBA", (tile_size, tile_size), (0, 0, 0, 0))
            else:
                raise ValueError(f"Unexpected glyph size for {glyph_path.name}: {glyph_img.size}")

        sheets[sheet_index].paste(glyph_img, (x, y))
        width_value = widths.get(key)
        if width_value is None:
            raise KeyError(f"Width missing for {key} in {widths_path}")

        sheet_key = f"glyph{sheet_index}"
        if sheet_key not in charmap:
            charmap[sheet_key] = {}
        charmap[sheet_key][key] = {"x": x, "y": y, "width": width_value}

    for i, sheet in enumerate(sheets):
        sheet_path = output_dir / f"glyph{i}.png"
        sheet.save(sheet_path)

    with charmap_path.open("w", encoding="utf-8") as f:
        json.dump(charmap, f, ensure_ascii=False, indent=4)

def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Pack glyph PNGs into sprite sheets")
    parser.add_argument("--input-dir", type=Path, default=Path("output_textures"), help="Directory with 16x16 glyph PNGs")
    parser.add_argument("--widths", type=Path, default=Path("widths.json"), help="Path to widths.json")
    parser.add_argument("--output-dir", type=Path, default=Path("atlas"), help="Where to write glyph sheets")
    parser.add_argument("--charmap", type=Path, default=Path("charmap.json"), help="Path for generated charmap JSON")
    parser.add_argument("--sheet-width", type=int, default=512, help="Sheet width in pixels")
    parser.add_argument("--sheet-height", type=int, default=512, help="Sheet height in pixels")
    parser.add_argument("--tile-size", type=int, default=16, help="Glyph tile size in pixels")
    return parser.parse_args()

def main() -> None:
    args = parse_args()
    pack_glyphs(
        input_dir=args.input_dir,
        widths_path=args.widths,
        output_dir=args.output_dir,
        charmap_path=args.charmap,
        sheet_width=args.sheet_width,
        sheet_height=args.sheet_height,
        tile_size=args.tile_size,
    )

if __name__ == "__main__":
    main()
