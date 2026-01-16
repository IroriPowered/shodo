import os
import json
from PIL import Image

INPUT_FILE = "input_characters.txt"
OUTPUT_DIR = "output_textures"
WIDTHS_JSON_FILE = "widths.json"
OVERRIDES_FILE = "width_overrides.json"

HEX_FILE_ALL = "unifont_all-17.0.03.hex"
HEX_FILE_JP = "unifont_jp-17.0.03.hex"

DEFAULT_OVERRIDES_DATA = []

def ensure_overrides_file():
    if not os.path.exists(OVERRIDES_FILE):
        print(f"Creating default {OVERRIDES_FILE}...")
        with open(OVERRIDES_FILE, 'w', encoding='utf-8') as f:
            json.dump(DEFAULT_OVERRIDES_DATA, f, indent=4, ensure_ascii=False)

def load_overrides():
    ensure_overrides_file()
    with open(OVERRIDES_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    parsed_ranges = []
    for item in data:
        start_char = item.get("from")
        end_char = item.get("to")
        l_val = item.get("left")
        r_val = item.get("right")
        
        if start_char and end_char and l_val is not None and r_val is not None:
            parsed_ranges.append({
                "start": ord(start_char),
                "end": ord(end_char),
                "left": int(l_val),
                "right": int(r_val)
            })
    return parsed_ranges

def get_override_bounds(codepoint, overrides):
    for rng in overrides:
        if rng["start"] <= codepoint <= rng["end"]:
            return (rng["left"], rng["right"])
    return None

def parse_hex_file(filepath):
    glyphs = {}
    if not os.path.exists(filepath):
        print(f"Warning: File {filepath} not found. Skipping.")
        return glyphs
    print(f"Loading {filepath}...")
    with open(filepath, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line: continue
            parts = line.split(':')
            if len(parts) != 2: continue
            glyphs[int(parts[0], 16)] = parts[1]
    return glyphs

def hex_to_raw_image(hex_bitmap):
    length = len(hex_bitmap)
    if length == 32: width = 8
    elif length == 64: width = 16
    else: return None

    height = 16
    img = Image.new('RGBA', (width, height), (255, 255, 255, 0))
    pixels = img.load()
    chars_per_row = width // 4
    
    for y in range(height):
        start = y * chars_per_row
        row_hex = hex_bitmap[start : start + chars_per_row]
        row_bin = bin(int(row_hex, 16))[2:].zfill(width)
        for x in range(width):
            if row_bin[x] == '1':
                pixels[x, y] = (255, 255, 255, 255)
    return img

def get_actual_bounds(img):
    width, height = img.size
    pixels = img.load()
    
    min_x = None
    max_x = None
    
    for x in range(width):
        has_pixel = False
        for y in range(height):
            if pixels[x, y][3] > 0:
                has_pixel = True
                break
        
        if has_pixel:
            if min_x is None: min_x = x
            max_x = x
            
    return (min_x, max_x)

def process_and_shift_image(raw_img, codepoint, overrides):
    org_width, height = raw_img.size
    
    calc_min, calc_max = get_actual_bounds(raw_img)
    override_bounds = get_override_bounds(codepoint, overrides)
    
    if override_bounds:
        target_left, target_right = override_bounds
        
        if target_left < 0: target_left = 0
        if target_right >= org_width: target_right = org_width - 1
        
        start_x = target_left
        end_x = target_right
    else:
        if calc_min is None:
            return raw_img, org_width 
        
        start_x = calc_min
        end_x = calc_max

    final_img = Image.new('RGBA', (16, 16), (255, 255, 255, 0))
    
    crop_box = (start_x, 0, end_x + 1, height)
    cropped_content = raw_img.crop(crop_box)
    
    final_img.paste(cropped_content, (0, 0))
    
    visual_width = end_x - start_x + 1
    
    final_img = Image.new('RGBA', (16, 16), (255, 255, 255, 0))
    final_img.paste(cropped_content, (0, 0))
    
    optimized_img = final_img.quantize(colors=2, method=2)
    
    visual_width = end_x - start_x + 1
    return optimized_img, visual_width

def main():
    glyph_db = parse_hex_file(HEX_FILE_ALL)
    glyph_db.update(parse_hex_file(HEX_FILE_JP))
    overrides = load_overrides()
    
    print(f"Loaded {len(glyph_db)} glyphs.")

    if not os.path.exists(OUTPUT_DIR): os.makedirs(OUTPUT_DIR)
    
    if not os.path.exists(INPUT_FILE):
        print(f"Error: {INPUT_FILE} missing.")
        return

    with open(INPUT_FILE, 'r', encoding='utf-8') as f:
        unique_chars = sorted(list(set(f.read())))

    width_map = {}
    processed_count = 0
    
    print("Processing...")
    
    for char in unique_chars:
        if ord(char) < 32: continue
        
        codepoint = ord(char)
        char_key = f"u{codepoint:04X}"

        if codepoint in glyph_db:
            raw_img = hex_to_raw_image(glyph_db[codepoint])
            
            if raw_img:
                final_img, char_width = process_and_shift_image(raw_img, codepoint, overrides)
                
                save_path = os.path.join(OUTPUT_DIR, f"{char_key}.png")
                final_img.save(save_path)
                
                width_map[char_key] = char_width
                processed_count += 1
        else:
            print(f"Missing: {char} ({char_key})")

    with open(WIDTHS_JSON_FILE, 'w', encoding='utf-8') as f:
        json.dump(width_map, f, indent=4, sort_keys=True)

    print(f"Done. Processed {processed_count} chars.")

if __name__ == "__main__":
    main()