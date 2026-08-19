import struct
import shutil
import os

def patch_file(filename, cp_method_idx, new_class_idx, code_search, code_replace):
    bak_name = filename + '.bak'
    if not os.path.exists(bak_name):
        shutil.copyfile(filename, bak_name)
        print(f'Created backup {bak_name}')
    
    with open(bak_name, 'rb') as f:
        data = bytearray(f.read())
        
    magic, minor, major, cp_count = struct.unpack('>IHHH', data[:10])
    pos = 10
    cp = [None]
    i = 1
    cp_positions = [None]
    while i < cp_count:
        cp_positions.append(pos)
        tag = data[pos]
        pos += 1
        if tag == 1:
            length, = struct.unpack('>H', data[pos:pos+2])
            pos += 2 + length
            cp.append(('Utf8',))
        elif tag in (7, 8):
            pos += 2
            cp.append(('Idx2',))
        elif tag in (9, 10, 11):
            pos += 4
            cp.append(('Ref',))
        elif tag == 12:
            pos += 4
            cp.append(('NameAndType',))
        elif tag in (3, 4):
            pos += 4
            cp.append(('Value4',))
        elif tag in (5, 6):
            pos += 8
            cp.append(('Value8',))
            cp.append(None)
            cp_positions.append(None)
            i += 1
        i += 1
        
    # Patch constant pool entry for MethodRef
    method_ref_pos = cp_positions[cp_method_idx]
    assert data[method_ref_pos] == 10, f"Expected tag 10 (MethodRef) at #{cp_method_idx}"
    struct.pack_into('>H', data, method_ref_pos + 1, new_class_idx)
    print(f'Patched #{cp_method_idx} in {filename} to point to Class #{new_class_idx}')
    
    # Patch bytecode
    code_pos = data.find(code_search)
    assert code_pos != -1, f"Could not find code_search in {filename}"
    assert len(code_search) == len(code_replace), f"Replacement length mismatch: {len(code_search)} vs {len(code_replace)}"
    data[code_pos:code_pos+len(code_replace)] = code_replace
    print(f'Patched bytecode in {filename} at offset {code_pos}')
    
    with open(filename, 'wb') as f:
        f.write(data)
    print(f'Successfully wrote patched {filename}')

# Patch f.class:
# cp_method_idx = 339
# new_class_idx = 98 (Image)
# code: 12 46 b6 01 58 2a b6 01 53 b0 -> 2a b8 01 53 b0 00 00 00 00 00
patch_file('f.class', 339, 98,
           bytes.fromhex('1246b601582ab60153b0'),
           bytes.fromhex('2ab80153b00000000000'))

# Patch g.class preserving branch target 62 (aload_1; areturn):
# cp_method_idx = 195
# new_class_idx = 40 (Image)
# code: 12 06 b6 00 c9 2a b6 00 c3 59 4b 4c 2b b0
# ->    2a b8 00 c3 00 00 00 00 00 59 4b 4c 2b b0
patch_file('g.class', 195, 40,
           bytes.fromhex('1206b600c92ab600c3594b4c2bb0'),
           bytes.fromhex('2ab800c30000000000594b4c2bb0'))
